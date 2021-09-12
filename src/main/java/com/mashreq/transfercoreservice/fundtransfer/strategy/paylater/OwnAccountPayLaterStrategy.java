package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.OWN_ACCOUNT_PL_SI_CREATION;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
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
import com.mashreq.transfercoreservice.fundtransfer.strategy.OwnAccountStrategy;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountFreezeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.FinTxnNoValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.SameAccountValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.service.DigitalUserSegment;
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
public class OwnAccountPayLaterStrategy extends OwnAccountStrategy {
	private final FundTransferOrderRepository fundTransferOrderRepository;
	private final SequenceNumberGenerator seqGenerator;

	@Autowired
	public OwnAccountPayLaterStrategy(AccountBelongsToCifValidator accountBelongsToCifValidator,
			SameAccountValidator sameAccountValidator, FinTxnNoValidator finTxnNoValidator,
			CurrencyValidator currencyValidator, LimitValidator limitValidator, AccountService accountService,
			DealValidator dealValidator, MaintenanceService maintenanceService,
			FundTransferMWService fundTransferMWService, BalanceValidator balanceValidator,
			NotificationService notificationService, AsyncUserEventPublisher auditEventPublisher,
			DigitalUserSegment digitalUserSegment, AccountFreezeValidator freezeValidator,
			PostTransactionService postTransactionService, FundTransferOrderRepository fundTransferOrderRepository,
			SequenceNumberGenerator seqGenerator, CCTransactionEligibilityValidator ccTrxValidator) {
		super(accountBelongsToCifValidator, sameAccountValidator, finTxnNoValidator, currencyValidator, limitValidator,
				accountService, dealValidator, maintenanceService, fundTransferMWService, balanceValidator,
				notificationService, auditEventPublisher, digitalUserSegment, freezeValidator,
				postTransactionService, ccTrxValidator);
		this.fundTransferOrderRepository = fundTransferOrderRepository;
		this.seqGenerator = seqGenerator;
	}

	@Override
	protected void validateAccountBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validateAccountContext, final BigDecimal transferAmountInSrcCurrency) {
		log.info("Skipping balance validation for pay later transaction");
	}

	@Override
	protected void prepareAndCallPostTransactionActivity(RequestMetaData metadata,
			FundTransferRequest fundTransferRequest, FundTransferRequestDTO request,
			FundTransferResponse fundTransferResponse, CurrencyConversionDto conversionResult) {
		if (isSuccess(fundTransferResponse)) {
			fundTransferRequest.setTransferType(OWN_ACCOUNT);
			fundTransferRequest.setNotificationType(NotificationType.LOCAL);
			fundTransferRequest.setStatus(MwResponseStatus.S.getName());
			this.getPostTransactionService().performPostTransactionActivities(metadata, fundTransferRequest, request);
		}
	}

	@Override
	protected void handleSuccessfulTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
			UserDTO userDTO, BigDecimal transactionAmount, final LimitValidatorResponse validationResult,
			final FundTransferResponse fundTransferResponse, final FundTransferRequest fundTransferRequest) {
		if (isSuccess(fundTransferResponse)) {
			final CustomerNotification customerNotification = this.populateCustomerNotification(
					validationResult.getTransactionRefNo(), request, transactionAmount, metadata, fundTransferRequest.getBeneficiaryFullName(), fundTransferRequest.getToAccount());
			this.getNotificationService().sendNotifications(customerNotification, OWN_ACCOUNT_PL_SI_CREATION, metadata,
					userDTO);
		}
	}

	@Override
	protected FundTransferResponse processTransfer(RequestMetaData metadata,
			final LimitValidatorResponse validationResult, final FundTransferRequest fundTransferRequest,
			FundTransferRequestDTO request) {
		log.info("Persisting funds transfer order for {}");
		FundTransferOrder fundTransferOrder = this.createOrderFromRequest(fundTransferRequest, metadata,
				validationResult.getTransactionRefNo(), request);
		log.info("Persisting funds transfer order for {}", HtmlEscapeCache.htmlEscape(fundTransferOrder));
		fundTransferOrderRepository.saveAndFlush(fundTransferOrder);
		return FundTransferResponse.builder().payOrderInitiated(true).transactionRefNo(fundTransferOrder.getOrderId()).build();
	}

	private boolean isSuccess(FundTransferResponse response) {
		return response.isPayOrderInitiated();
	}

	private FundTransferOrder createOrderFromRequest(FundTransferRequest fundTransferRequest, RequestMetaData metadata,
			String txnRefNo, FundTransferRequestDTO request) {
		FundTransferOrder order = new FundTransferOrder();
		order.setCreatedBy(metadata.getUsername());
		order.setCreatedOn(LocalDateTime.now());
		order.setChannel(metadata.getChannel());
		order.setCif(metadata.getPrimaryCif());
		order.setUserType(metadata.getUserType());
		order.setCustomerSegment(metadata.getSegment());
		order.setDealRate(fundTransferRequest.getDealRate());
		order.setDestinationAccountCurrency(fundTransferRequest.getDestinationCurrency());
		order.setFinancialTransactionNo(fundTransferRequest.getFinTxnNo());
		order.setFrequency(
				request.getFrequency() != null ? SIFrequencyType.getSIFrequencyTypeByName(request.getFrequency())
						: null);
		order.setFxDealNumber(fundTransferRequest.getDealNumber());
		order.setInternalAccFlag(fundTransferRequest.getInternalAccFlag());
		order.setOrderType(FTOrderType.getFTOrderTypeByName(request.getOrderType()));
		order.setOrderId(order.getOrderType().isRepeateable() ? seqGenerator.getNextOrderId() : txnRefNo);
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTrxRefNo(txnRefNo);
		order.setProductId(fundTransferRequest.getProductId());
		order.setPurposeCode(request.getPurposeCode());
		order.setPurposeDesc(request.getPurposeDesc());
		order.setServiceType(ServiceType.WYMA);
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
		order.setTransactionValue(Money.valueOf(request.getAmount(), fundTransferRequest.getTxnCurrency()));
		order.setPaymentNote(request.getPaymentNote());
		order.setSourceAccount(request.getFromAccount());
		order.setEmail(metadata.getEmail());
		order.setNextExecutionTime(OrderExecutionDateResolver.getNextExecutionTime(order));
		return order;
	}
	
	@Override
	protected FundTransferResponse prepareResponse(final BigDecimal transferAmountInSrcCurrency,
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .debitAmount(transferAmountInSrcCurrency)
                .build();
	}
}
