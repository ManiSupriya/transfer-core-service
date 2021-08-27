package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.LOCAL_PL_SI_CREATION;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.common.HtmlEscapeCache;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferCCMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.LocalFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCBalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.FinTxnNoValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.IBANValidator;
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
import com.mashreq.transfercoreservice.repository.CountryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LocalFundPayLaterTransferStrategy extends LocalFundTransferStrategy {
	private final FundTransferOrderRepository fundTransferOrderRepository;
	private final SequenceNumberGenerator seqGenerator;
	public LocalFundPayLaterTransferStrategy(IBANValidator ibanValidator, FinTxnNoValidator finTxnNoValidator,
			AccountBelongsToCifValidator accountBelongsToCifValidator, CCBelongsToCifValidator ccBelongsToCifValidator,
			BeneficiaryValidator beneficiaryValidator, AccountService accountService,
			BeneficiaryService beneficiaryService, LimitValidator limitValidator,
			FundTransferMWService fundTransferMWService, PaymentPurposeValidator paymentPurposeValidator,
			BalanceValidator balanceValidator, CCBalanceValidator ccBalanceValidator,
			MaintenanceService maintenanceService, MobCommonService mobCommonService, DealValidator dealValidator,
			CountryRepository countryRepository, FundTransferCCMWService fundTransferCCMWService,
			AsyncUserEventPublisher auditEventPublisher, NotificationService notificationService,
											 QRDealsService qrDealsService, CardService cardService,
											 PostTransactionService postTransactionService,
			FundTransferOrderRepository fundTransferOrderRepository,
			SequenceNumberGenerator seqGenerator,
			CCTransactionEligibilityValidator ccTrxValidator) {
		super(ibanValidator, finTxnNoValidator, accountBelongsToCifValidator, ccBelongsToCifValidator, beneficiaryValidator,
				accountService, beneficiaryService, limitValidator, fundTransferMWService, paymentPurposeValidator,
				balanceValidator, ccBalanceValidator, maintenanceService, mobCommonService, dealValidator, countryRepository,
				fundTransferCCMWService, auditEventPublisher, notificationService, qrDealsService, cardService, postTransactionService,ccTrxValidator);
		this.fundTransferOrderRepository=fundTransferOrderRepository;
		this.seqGenerator = seqGenerator;
	}
	
	 /**
     * Method is used to initiate the Fund transfer for the Credit card
     * @param request
     * @param requestMetaData
     * @param userDTO
     * @return
     */
    protected FundTransferResponse executeCC(FundTransferRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO){
    	/** this will throw exception */
    	GenericExceptionHandler.handleError(TransferErrorCode.TXN_NOT_ALLOWED_FOR_PAY_LATER,
				TransferErrorCode.TXN_NOT_ALLOWED_FOR_PAY_LATER.getErrorMessage(),
				TransferErrorCode.TXN_NOT_ALLOWED_FOR_PAY_LATER.getErrorMessage());
    	/** is never gonna invoke this */
    	return null;
    }

    @Override
    protected void handleSuccessfulTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
			UserDTO userDTO, final LimitValidatorResponse validationResult,
			final FundTransferRequest fundTransferRequest, final FundTransferResponse fundTransferResponse) {
    	//TODO:change notification and post transaction events
		if(isSuccess(fundTransferResponse)){
            final CustomerNotification customerNotification = this.populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),
					request.getAmount(),fundTransferRequest.getBeneficiaryFullName(),fundTransferRequest.getToAccount());
            getNotificationService().sendNotifications(customerNotification, LOCAL_PL_SI_CREATION, metadata, userDTO);
            fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getTxnCurrency()));
            fundTransferRequest.setNotificationType(LOCAL_PL_SI_CREATION);
            fundTransferRequest.setStatus(MwResponseStatus.S.getName());
            getPostTransactionService().performPostTransactionActivities(metadata, fundTransferRequest, request);
        }
	}
    
    private boolean isSuccess(FundTransferResponse response) {
		return Boolean.TRUE.equals(response.getPayOrderInitiated());
	}
    
    @Override
    protected BigDecimal validateBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validationContext, final AccountDetailsDTO fromAccountDetails,
			final BeneficiaryDto beneficiaryDto) {
    	log.info("Skipping balance validation check for loal pay later transaction ");
    	final BigDecimal transferAmountInSrcCurrency = this.getAmountInSrcCurrency(request, beneficiaryDto, fromAccountDetails);
		return transferAmountInSrcCurrency;
	}
    
    protected FundTransferResponse processTransaction(RequestMetaData metadata, String txnRefNo,
			final FundTransferRequest fundTransferRequest,FundTransferRequestDTO request) {
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
		order.setChargeBearer(ChargeBearer.valueOf(request.getChargeBearer()));
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
		order.setServiceType(ServiceType.LOCAL);
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
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult, String txnRefNo,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .debitAmount(transferAmountInSrcCurrency).build();
	}
}
