package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.WITHIN_MASHREQ_PL_SI_CREATION;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.HtmlEscapeCache;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.WithinMashreqStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.MashreqUAEAccountNumberResolver;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountFreezeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.FinTxnNoValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.SameAccountValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.OrderStatus;
import com.mashreq.transfercoreservice.paylater.enums.SIFrequencyType;
import com.mashreq.transfercoreservice.paylater.model.FundTransferOrder;
import com.mashreq.transfercoreservice.paylater.model.Money;
import com.mashreq.transfercoreservice.paylater.repository.FundTransferOrderRepository;
import com.mashreq.transfercoreservice.paylater.utils.DateTimeUtil;
import com.mashreq.transfercoreservice.paylater.utils.OrderExecutionDateResolver;
import com.mashreq.transfercoreservice.paylater.utils.SequenceNumberGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WithinMashreqPayLaterStrategy extends WithinMashreqStrategy {
	private final FundTransferOrderRepository fundTransferOrderRepository;
	private final SequenceNumberGenerator seqGenerator;
	@Autowired
	public WithinMashreqPayLaterStrategy(SameAccountValidator sameAccountValidator, FinTxnNoValidator finTxnNoValidator,
			AccountBelongsToCifValidator accountBelongsToCifValidator, CurrencyValidator currencyValidator,
			BeneficiaryValidator beneficiaryValidator, AccountService accountService,
			BeneficiaryService beneficiaryService, LimitValidator limitValidator, MaintenanceService maintenanceService,
			FundTransferMWService fundTransferMWService, BalanceValidator balanceValidator, DealValidator dealValidator,
			AsyncUserEventPublisher auditEventPublisher, NotificationService notificationService,
			AccountFreezeValidator freezeValidator, MashreqUAEAccountNumberResolver accountNumberResolver,
			PostTransactionService postTransactionService,
			FundTransferOrderRepository fundTransferOrderRepository,
			SequenceNumberGenerator seqGenerator, CCTransactionEligibilityValidator ccTrxValidator) {
		super(sameAccountValidator, finTxnNoValidator, accountBelongsToCifValidator, currencyValidator, beneficiaryValidator,
				accountService, beneficiaryService, limitValidator, maintenanceService, fundTransferMWService, balanceValidator,
				dealValidator, auditEventPublisher, notificationService, 
				freezeValidator, accountNumberResolver, postTransactionService, ccTrxValidator);
		this.fundTransferOrderRepository = fundTransferOrderRepository;
		this.seqGenerator = seqGenerator;
	}

	@Override
	protected void validateAccountBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validationContext) {
		log.info("Skipping balance validation for pay later within Mashreq fund transfer");
	}
	@Override
	protected void handleSuccessfulTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
			UserDTO userDTO, final LimitValidatorResponse validationResult,
			final FundTransferRequest fundTransferRequest, final FundTransferResponse fundTransferResponse) {
		//TODO: Change this accordingly for pay later
		if(isSuccess(fundTransferResponse)) {
        	final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),
					request.getAmount(),fundTransferRequest.getBeneficiaryFullName(),fundTransferRequest.getToAccount());
            this.getNotificationService().sendNotifications(customerNotification, WITHIN_MASHREQ_PL_SI_CREATION, metadata, userDTO);
            fundTransferRequest.setTransferType(MASHREQ);
            fundTransferRequest.setNotificationType(NotificationType.LOCAL);
            fundTransferRequest.setStatus(MwResponseStatus.S.getName());
            this.getPostTransactionService().performPostTransactionActivities(metadata, fundTransferRequest);
        }
	}
	
	private boolean isSuccess(FundTransferResponse response) {
		return Boolean.TRUE.equals(response.getPayOrderInitiated());
	}
	
	@Override
	protected FundTransferResponse processTransaction(RequestMetaData metadata, String txnRefNo,
			final FundTransferRequest fundTransferRequest, FundTransferRequestDTO request) {
		log.info("Persisting funds transfer order for {}");
		FundTransferOrder fundTransferOrder = this.createOrderFromRequest(fundTransferRequest, metadata, txnRefNo,
				request);
		log.info("Persisting funds transfer order for {}", HtmlEscapeCache.htmlEscape(fundTransferOrder));
		fundTransferOrderRepository.saveAndFlush(fundTransferOrder);
		return FundTransferResponse.builder().payOrderInitiated(true).transactionRefNo(fundTransferOrder.getOrderId()).build();
		}
	
	private FundTransferOrder createOrderFromRequest(FundTransferRequest fundTransferRequest, RequestMetaData metadata,
			String txnRefNo, FundTransferRequestDTO request) {
		FundTransferOrder order = new FundTransferOrder();
		order.setCreatedBy(metadata.getUsername());
		order.setCreatedOn(LocalDateTime.now());
		order.setBeneficiaryId(Long.valueOf(request.getBeneficiaryId()));
		order.setChannel(metadata.getChannel());
		order.setCif(metadata.getPrimaryCif());
		order.setUserType(metadata.getUserType());
		order.setCustomerSegment(metadata.getSegment());
		order.setDealRate(fundTransferRequest.getDealRate());
		order.setDestinationAccountCurrency(fundTransferRequest.getDestinationCurrency());
		order.setFinancialTransactionNo(fundTransferRequest.getFinTxnNo());
		order.setFrequency(request.getFrequency()!= null ? SIFrequencyType.getSIFrequencyTypeByName(request.getFrequency()) : null);
		order.setFxDealNumber(fundTransferRequest.getDealNumber());
		order.setInternalAccFlag(fundTransferRequest.getInternalAccFlag());
		order.setOrderStatus(OrderStatus.PENDING);
		order.setOrderType(FTOrderType.getFTOrderTypeByName(request.getOrderType()));
		order.setOrderId(order.getOrderType().isRepeateable() ? seqGenerator.getNextOrderId() : txnRefNo);
		order.setTrxRefNo(txnRefNo);
		order.setProductId(fundTransferRequest.getProductId());
		order.setPurposeCode(request.getPurposeCode());
		order.setPurposeDesc(request.getPurposeDesc());
		order.setServiceType(ServiceType.WAMA);
		order.setSourceCurrency(fundTransferRequest.getSourceCurrency());
		order.setStartDate(
				DateTimeUtil.getInstance().convertToDate(request.getStartDate(), DateTimeUtil.DATE_TIME_FORMATTER).atTime(0, 0));
		if (FTOrderType.SI.equals(order.getOrderType())) {
			order.setEndDate(
					DateTimeUtil.getInstance().convertToDate(request.getEndDate(), DateTimeUtil.DATE_TIME_FORMATTER).atTime(23, 59));
		}
		order.setSourceBranchCode(fundTransferRequest.getSourceBranchCode());
		order.setDestinationAccountNumber(fundTransferRequest.getToAccount());
		order.setTransactionCode(fundTransferRequest.getTransactionCode());
		order.setTransactionValue(Money.valueOf(request.getAmount(), request.getTxnCurrency()));
		order.setPaymentNote(request.getPaymentNote());
		order.setSourceAccount(request.getFromAccount());
		order.setEmail(metadata.getEmail());
		order.setNextExecutionTime(OrderExecutionDateResolver.getNextExecutionTime(order));
		return order;
	}
	
	@Override
	protected FundTransferResponse prepareResponse(final BigDecimal transferAmountInSrcCurrency,
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult, String txnRefNo,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .debitAmount(transferAmountInSrcCurrency)
                .build();
	}
}
