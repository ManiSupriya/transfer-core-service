package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.INFT_PL_SI_CREATION;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.HtmlEscapeCache;
import com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.InternationalFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.PaymentPurposeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
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
@Service("internationalPayLaterFundTransferStrategy")
public class InternationalPayLaterFundTransferStrategy extends InternationalFundTransferStrategy {
	private final FundTransferOrderRepository fundTransferOrderRepository;
	private final SequenceNumberGenerator seqGenerator;
	public InternationalPayLaterFundTransferStrategy(AccountService accountService,
			AccountBelongsToCifValidator accountBelongsToCifValidator, PaymentPurposeValidator paymentPurposeValidator,
			BeneficiaryValidator beneficiaryValidator, BalanceValidator balanceValidator,
			FundTransferMWService fundTransferMWService, MaintenanceService maintenanceService,
			MobCommonService mobCommonService, DealValidator dealValidator, NotificationService notificationService,
			BeneficiaryService beneficiaryService, LimitValidator limitValidator,
			CCTransactionEligibilityValidator ccTrxValidator,
			FundTransferOrderRepository fundTransferOrderRepository,
			SequenceNumberGenerator seqGenerator,
			CurrencyValidator currencyValidator,MinTransactionAmountValidator minTransactionAmountValidator) {
		super(accountService, accountBelongsToCifValidator, paymentPurposeValidator, beneficiaryValidator,
				balanceValidator, fundTransferMWService, maintenanceService, mobCommonService, dealValidator,
				notificationService, beneficiaryService, limitValidator,
				ccTrxValidator, currencyValidator, minTransactionAmountValidator);

		this.fundTransferOrderRepository=fundTransferOrderRepository;
		this.seqGenerator=seqGenerator;
	}

	@Override
	protected CurrencyConversionDto validateAccountBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validationContext, BeneficiaryDto beneficiaryDto,
			final AccountDetailsDTO sourceAccountDetailsDTO) {
		log.info("Skipping balance validation check for pay later transaction ");
		return getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);
	}
	
	@Override
	protected FundTransferResponse processTransaction(RequestMetaData metadata, String txnRefNo,
			final FundTransferRequest fundTransferRequest, FundTransferRequestDTO request) {
		log.info("Persisting funds transfer order");
		FundTransferOrder fundTransferOrder = this.createOrderFromRequest(fundTransferRequest,metadata,txnRefNo,request);
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
		order.setChargeBearer(request.getChargeBearer());
		order.setCif(metadata.getPrimaryCif());
		order.setUserType(metadata.getUserType());
		order.setCustomerSegment(metadata.getSegment());
		order.setDealRate(fundTransferRequest.getDealRate());
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
		order.setServiceType(ServiceType.INFT);
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
		order.setNextExecutionTime(OrderExecutionDateResolver.getNextExecutionTime(order));
		order.setEmail(metadata.getEmail());
		return order;
	}

	@Override
	protected void handleSuccessfullTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
                                                UserDTO userDTO, final LimitValidatorResponse validationResult,
                                                final FundTransferRequest fundTransferRequest, final FundTransferResponse fundTransferResponse, BeneficiaryDto beneficiaryDto) {
		if(isSuccess(fundTransferResponse)){
        final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),
				request.getTxnCurrency(),request.getAmount(),fundTransferRequest.getBeneficiaryFullName(),fundTransferRequest.getToAccount());
        getNotificationService().sendNotifications(customerNotification, INFT_PL_SI_CREATION, metadata, userDTO);
        fundTransferRequest.setTransferType(INTERNATIONAL);
        fundTransferRequest.setNotificationType(INFT_PL_SI_CREATION);
        fundTransferRequest.setStatus(MwResponseStatus.S.getName());
        this.getPostTransactionService().performPostTransactionActivities(metadata, fundTransferRequest, request, ofNullable(beneficiaryDto));
        }
	}

	private boolean isSuccess(FundTransferResponse response) {
		return response.isPayOrderInitiated();
	}

	@Override
	protected FundTransferResponse prepareResponse(final BigDecimal transferAmountInSrcCurrency,
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult, String txnRefNo,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .debitAmount(transferAmountInSrcCurrency)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
	}
	
}
