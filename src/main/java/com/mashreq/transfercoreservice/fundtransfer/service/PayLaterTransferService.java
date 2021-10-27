package com.mashreq.transfercoreservice.fundtransfer.service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumMap;

import javax.annotation.PostConstruct;

import com.mashreq.transfercoreservice.repository.QrStatusMsRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.CharityStrategyDefault;
import com.mashreq.transfercoreservice.fundtransfer.strategy.InternationalFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.LocalFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.OwnAccountStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.WithinMashreqStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.InternationalPayLaterFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.LocalFundPayLaterTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.OwnAccountPayLaterStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.WithinMashreqPayLaterStrategy;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.promo.service.PromoCodeService;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistory;
import com.mashreq.transfercoreservice.transactionqueue.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TrackExec
@Service
@Qualifier("payLaterTransferService")
public class PayLaterTransferService extends FundTransferServiceDefault{
	private final OwnAccountPayLaterStrategy ownAccountPayLaterStrategy;
	private final WithinMashreqPayLaterStrategy withinMashreqPayLaterStrategy;
	private final LocalFundPayLaterTransferStrategy localFundPayLaterTransferStrategy;
	private final InternationalPayLaterFundTransferStrategy internationalPayLaterFundTransferStrategy;
    @Autowired
	public PayLaterTransferService(DigitalUserRepository digitalUserRepository,
                                   TransactionRepository transactionRepository, DigitalUserLimitUsageService digitalUserLimitUsageService,
                                   OwnAccountStrategy ownAccountStrategy, WithinMashreqStrategy withinMashreqStrategy,
                                   LocalFundTransferStrategy localFundTransferStrategy,
                                   InternationalFundTransferStrategy internationalFundTransferStrategy,
                                   CharityStrategyDefault charityStrategyDefault, AsyncUserEventPublisher auditEventPublisher,
                                   OTPService otpService, ExternalErrorCodeConfig errorCodeConfig,
                                   OwnAccountPayLaterStrategy ownAccountPayLaterStrategy,
                                   WithinMashreqPayLaterStrategy withinMashreqPayLaterStrategy,
                                   LocalFundPayLaterTransferStrategy localFundPayLaterTransferStrategy,
                                   InternationalPayLaterFundTransferStrategy internationalPayLaterFundTransferStrategy,
                                   PromoCodeService promoCodeService, MobCommonService mobCommonService) {
		super(digitalUserRepository, transactionRepository, digitalUserLimitUsageService, ownAccountStrategy,
				withinMashreqStrategy, localFundTransferStrategy, internationalFundTransferStrategy,
				charityStrategyDefault, auditEventPublisher, otpService, errorCodeConfig,
				promoCodeService, mobCommonService);
		this.ownAccountPayLaterStrategy = ownAccountPayLaterStrategy;
		this.withinMashreqPayLaterStrategy = withinMashreqPayLaterStrategy;
		this.localFundPayLaterTransferStrategy = localFundPayLaterTransferStrategy;
		this.internationalPayLaterFundTransferStrategy = internationalPayLaterFundTransferStrategy;
	}
    
    @Override
    @PostConstruct
    public void init() {
        this.fundTransferStrategies = new EnumMap<>(ServiceType.class);
        this.fundTransferStrategies.put(WYMA, ownAccountPayLaterStrategy);
        this.fundTransferStrategies.put(WAMA, withinMashreqPayLaterStrategy);
        this.fundTransferStrategies.put(LOCAL, localFundPayLaterTransferStrategy);
        this.fundTransferStrategies.put(INFT, internationalPayLaterFundTransferStrategy);
    }
    
    @Override
    protected void handleIfTransactionIsSuccess(RequestMetaData metadata, FundTransferRequestDTO request,
			UserDTO userDTO, FundTransferResponse response) {
    	//TODO: have to insert the usage while inititation
		if (isSuccessOrProcessing(response)) {
        	Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
            DigitalUserLimitUsageDTO digitalUserLimitUsageDTO = generateUserLimitUsage(
                    request.getServiceType(), response.getLimitUsageAmount(), userDTO, metadata, response.getLimitVersionUuid(),response.getTransactionRefNo(), bendId );
            log.info("Inserting into limits table {} ", digitalUserLimitUsageDTO);
            this.getDigitalUserLimitUsageService().insert(digitalUserLimitUsageDTO);
        }
	}
    
    @Override
    protected void handleFailure(FundTransferRequestDTO request, FundTransferResponse response) {
		if (isFailure(response)) {
			GenericExceptionHandler.handleError(TransferErrorCode.PAY_LATER_TRANSACTION_INITIATION_FAILED,
					TransferErrorCode.PAY_LATER_TRANSACTION_INITIATION_FAILED.getCustomErrorCode(),
					TransferErrorCode.PAY_LATER_TRANSACTION_INITIATION_FAILED.getErrorMessage());
		}
	}
    
    @Override
    protected TransactionHistory updateTransactionHistory(RequestMetaData metadata, FundTransferRequestDTO request,
			UserDTO userDTO, FundTransferResponse response) {
        TransactionHistory transactionHistory = generateTransactionHistory(request, response, userDTO, metadata);
        log.info("Skipping pay later insertion into table {} ", htmlEscape(transactionHistory.getTransactionRefNo()));
		return transactionHistory;
	}
    
    protected boolean isFailure(FundTransferResponse response) {
        return !response.isPayOrderInitiated();
    }

    protected boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.isPayOrderInitiated();
    }

    @Override
    protected String getFailureMessage(TransferErrorCode fundTransferFailed, FundTransferRequestDTO request, FundTransferResponse response) {
        return String.format("FIN-TXN-NO [%s] : REFERENCE-NO [%s] REFERENCE-MESSAGE [%s] : ",
                request.getFinTxnNo(),
                TransferErrorCode.PAY_LATER_TRANSACTION_INITIATION_FAILED.getCustomErrorCode(),
                fundTransferFailed.getErrorMessage()
        );
    }

    @Override
    protected String getInitiatedRemarks(FundTransferRequestDTO request) {
        return String.format("From Account = %s, To Account = %s, Amount = %s, Currency = %s, Financial Transaction Number = %s, Beneficiary Id = %s ",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getCurrency(),
                request.getFinTxnNo(),
                request.getBeneficiaryId());
    }

    @Override
    protected TransactionHistory generateTransactionHistory(FundTransferRequestDTO request, FundTransferResponse fundTransferResponse, UserDTO userDTO,
                                             RequestMetaData fundTransferMetadata) {
        //convert dto
        return TransactionHistory.builder()
                .cif(fundTransferMetadata.getPrimaryCif())
                .userId(userDTO.getUserId())
                .accountTo(request.getToAccount())
                .transactionTypeCode(request.getServiceType())
                .channel(fundTransferMetadata.getChannel())
                .ipAddress(fundTransferMetadata.getDeviceIP())
                .paidAmount(request.getAmount() == null ? request.getSrcAmount() : request.getAmount())
                .fromCurrency(request.getCurrency())
                .toCurrency(request.getTxnCurrency())
                .status(fundTransferResponse.isPayOrderInitiated() ?  MwResponseStatus.S.getName() : MwResponseStatus.F.getName())
                .accountFrom(request.getFromAccount())
                .financialTransactionNo(request.getFinTxnNo())
                .transactionRefNo(fundTransferResponse.getTransactionRefNo())
                .valueDate(LocalDateTime.now())
                .createdDate(Instant.now())
                //TODO: have to change this to numberutils.isCreatable
                .beneficiaryId(StringUtils.isNotBlank(request.getBeneficiaryId())?Long.valueOf(request.getBeneficiaryId()):null)
                .paymentNote(request.getPaymentNote())
                .build();

    }
}
