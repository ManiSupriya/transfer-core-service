package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
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
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.FinTxnNoValidator;
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

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class InternationalPayLaterFundTransferStrategy extends InternationalFundTransferStrategy {
	private final FundTransferOrderRepository fundTransferOrderRepository;
	public InternationalPayLaterFundTransferStrategy(FinTxnNoValidator finTxnNoValidator, AccountService accountService,
			AccountBelongsToCifValidator accountBelongsToCifValidator, PaymentPurposeValidator paymentPurposeValidator,
			BeneficiaryValidator beneficiaryValidator, BalanceValidator balanceValidator,
			FundTransferMWService fundTransferMWService, MaintenanceService maintenanceService,
			MobCommonService mobCommonService, DealValidator dealValidator, NotificationService notificationService,
			BeneficiaryService beneficiaryService, LimitValidator limitValidator,
			FundTransferOrderRepository fundTransferOrderRepository) {
		super(finTxnNoValidator, accountService, accountBelongsToCifValidator, paymentPurposeValidator, beneficiaryValidator,
				balanceValidator, fundTransferMWService, maintenanceService, mobCommonService, dealValidator,
				notificationService, beneficiaryService, limitValidator);
		this.fundTransferOrderRepository=fundTransferOrderRepository;
	}

	@Override
	protected BigDecimal validateAccountBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validationContext, BeneficiaryDto beneficiaryDto,
			final AccountDetailsDTO sourceAccountDetailsDTO) {
		final BigDecimal transferAmountInSrcCurrency = getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);
		log.info("Skipping balance validation check for loal pay later transaction ");
		return transferAmountInSrcCurrency;
	}
	
	@Override
	protected FundTransferResponse processTransaction(RequestMetaData metadata, String txnRefNo,
			final FundTransferRequest fundTransferRequest, FundTransferRequestDTO request) {
		log.info("Persisting funds transfer order for {}");
		FundTransferOrder fundTransferOrder = this.createOrderFromRequest(fundTransferRequest,metadata,txnRefNo,request);
		log.info("Persisting funds transfer order for {}",fundTransferOrder);
		fundTransferOrderRepository.saveAndFlush(fundTransferOrder);
		return FundTransferResponse.builder().payOrderInitiated(true).build();
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
		//order.setDestinationAccountCurrency(fundTransferRequest.get);
		order.setFinancialTransactionNo(fundTransferRequest.getFinTxnNo());
		order.setFrequency(request.getFrequency()!= null ? SIFrequencyType.getSIFrequencyTypeByName(request.getFrequency()) : null);
		order.setFxDealNumber(fundTransferRequest.getDealNumber());
		order.setInternalAccFlag(fundTransferRequest.getInternalAccFlag());
		//TODO: create an order id generator class with some common logic for all SI orders
		order.setOrderId(txnRefNo);
		order.setOrderStatus(OrderStatus.PENDING);
		order.setOrderType(FTOrderType.getFTOrderTypeByName(request.getOrderType()));
		order.setProductId(fundTransferRequest.getProductId());
		order.setPurposeCode(request.getPurposeCode());
		order.setPurposeDesc(request.getPurposeDesc());
		order.setServiceType(ServiceType.INFT);
		order.setSourceCurrency(fundTransferRequest.getSourceCurrency());
		order.setSndrBranchCode(fundTransferRequest.getSourceBranchCode());
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
		return order;
	}

	@Override
	protected void handleSuccessfullTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
			UserDTO userDTO, final LimitValidatorResponse validationResult,
			final FundTransferRequest fundTransferRequest, final FundTransferResponse fundTransferResponse) {
		if(isSuccessOrProcessing(fundTransferResponse)){
        final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),request.getAmount());
        getNotificationService().sendNotifications(customerNotification,OTHER_ACCOUNT_TRANSACTION,metadata,userDTO);
        fundTransferRequest.setTransferType(INTERNATIONAL);
        fundTransferRequest.setNotificationType(OTHER_ACCOUNT_TRANSACTION);
        fundTransferRequest.setStatus(MwResponseStatus.S.getName());
        this.getPostTransactionService().performPostTransactionActivities(metadata, fundTransferRequest);
        }
	}

	private boolean isSuccessOrProcessing(FundTransferResponse response) {
		return Boolean.TRUE.equals(response.getPayOrderInitiated());
	}

	
}
