package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapClient;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.middleware.enums.YesNo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

import static com.mashreq.transfercoreservice.middleware.SoapWebserviceClientFactory.soapClient;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;


@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferMWService {

	private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private static final String NARRATION_PREFIX = "Fund Transfer-";
    private static final String NARRATION_SUFFIX = " Banking";
    private static final String PAYMENT_DETAIL_PREFIX = "/REF/ ";
    public static final String SPACE_CHAR = " ";
    private final AsyncUserEventPublisher auditEventPublisher;
    private static final String GOLD = "XAU";
    private static final String SILVER = "XAG";
    private static final String UAE_COUNTRY= "UNITED ARAB EMIRATES";
    
    public static final String INTERNATIONAL = "International";
    
    public static final String NON_AED_LOCAL = "Local non-AED";
    

    
    public FundTransferResponse transfer(FundTransferRequest request, RequestMetaData metaData, String msgId) {
        log.info("Fund transfer initiated from account [ {} ]", htmlEscape(request.getFromAccount()));

        SoapClient soapClient = soapClient(soapServiceProperties,
                new Class[]{
                        HeaderType.class ,
                        EAIServices.class,
                        FundTransferReqType.class,
                        FundTransferResType.class,
                        ErrorType.class,
                });

        EAIServices response = (EAIServices) soapClient.exchange(generateEAIRequest(request,msgId));
        /**
         * Handling Null pointer Exception in case of Host Timeout 
         */
        if (!isSuccessfull(response) && ObjectUtils.isEmpty(response.getBody().getFundTransferReq())) {
        	log.info("Fund transfer failed to account [ {} ]", request.getToAccount());
        	auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.FUND_TRANSFER_MW_CALL, metaData, getRemarks(request), msgId,
        			response.getBody().getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getErrorDescription(), response.getBody().getExceptionDetails().getErrorCode());
        	GenericExceptionHandler.handleError(TransferErrorCode.EXTERNAL_SERVICE_ERROR_MW,
					TransferErrorCode.EXTERNAL_SERVICE_ERROR_MW.getErrorMessage(),
					response.getBody().getExceptionDetails().getErrorCode()+"-"+response.getBody().getExceptionDetails().getErrorDescription());
        }
        final FundTransferResType.Transfer transfer = response.getBody().getFundTransferRes().getTransfer().get(0);
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();
        if (isSuccessfull(response)) {
            auditEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.FUND_TRANSFER_MW_CALL, metaData, getRemarks(request), msgId);
            log.info("Fund transferred successfully to account [ {} ]", request.getToAccount());
            final CoreFundTransferResponseDto coreFundTransferResponseDto = constructFTResponseDTO(transfer, exceptionDetails, MwResponseStatus.S);
            return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
        }

        log.info("Fund transfer failed to account [ {} ]", request.getToAccount());
        final CoreFundTransferResponseDto coreFundTransferResponseDto = constructFTResponseDTO(transfer, exceptionDetails, MwResponseStatus.F);
        auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.FUND_TRANSFER_MW_CALL, metaData, getRemarks(request), msgId,
                coreFundTransferResponseDto.getMwResponseCode(), coreFundTransferResponseDto.getMwResponseDescription(), coreFundTransferResponseDto.getExternalErrorMessage());
        return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
    }
    
    private String getRemarks(FundTransferRequest request) {
        return String.format("From Account = %s, To Account = %s, Amount = %s, SrcAmount= %s, Destination Currency = %s, Source Currency = %s," +
                        " Financial Transaction Number = %s, Beneficiary full name = %s, Swift code= %s, Beneficiary bank branch = %s ",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getSrcAmount(),
                request.getDestinationCurrency(),
                request.getSourceCurrency(),
                request.getFinTxnNo(),
                request.getBeneficiaryFullName(),
                request.getAwInstBICCode(),
                request.getAwInstName());
    }

    private CoreFundTransferResponseDto constructFTResponseDTO(FundTransferResType.Transfer transfer, ErrorType exceptionDetails, MwResponseStatus s) {
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setHostRefNo(transfer.getTransactionRefNo());
        coreFundTransferResponseDto.setExternalErrorMessage(exceptionDetails.getData());
        coreFundTransferResponseDto.setMwReferenceNo(transfer.getTransactionRefNo());
        coreFundTransferResponseDto.setMwResponseDescription(exceptionDetails.getErrorDescription());
        coreFundTransferResponseDto.setMwResponseStatus(s);
        coreFundTransferResponseDto.setTransactionRefNo(transfer.getTransactionRefNo());
        coreFundTransferResponseDto.setMwResponseCode(exceptionDetails.getErrorCode());
                return coreFundTransferResponseDto;
    }

    private boolean isSuccessfull(EAIServices response) {
        log.info("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.error("Exception during fund transfer. Code: {} , Description: {}", response.getBody()
                    .getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getData());

            return false;
        }
        return true;
    }

    public EAIServices generateEAIRequest(FundTransferRequest request, String msgId) {

        //TODO remove this
        SecureRandom secureRandom = new SecureRandom();
        int batchTransIdTemporary = Math.abs((secureRandom.nextInt() * 9000) + 1000);

        EAIServices services = new EAIServices();
        services.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFundTransfer(),msgId));
        services.setBody(new EAIServices.Body());
        
        //Setting individual components
        FundTransferReqType fundTransferReqType = new FundTransferReqType();

        //TODO Change this to proper batch id
        fundTransferReqType.setBatchTransactionId(batchTransIdTemporary + "");
        fundTransferReqType.setProductId(request.getProductId());
        fundTransferReqType.setTransTypeCode(request.getPurposeCode());

        List<FundTransferReqType.Transfer> transferList = fundTransferReqType.getTransfer();
        FundTransferReqType.Transfer.CreditLeg creditLeg = new FundTransferReqType.Transfer.CreditLeg();
        FundTransferReqType.Transfer.DebitLeg debitLeg = new FundTransferReqType.Transfer.DebitLeg();

        debitLeg.setDebitRefNo(request.getFinTxnNo());
        debitLeg.setAccountNo(request.getFromAccount());
        debitLeg.setTransferBranch(request.getSourceBranchCode());
        debitLeg.setCurrency(request.getSourceCurrency());
        debitLeg.setNarration1(generateNarration(request.getChannel()));
        debitLeg.setInternalAccFlag(request.getInternalAccFlag());

        creditLeg.setAccountNo(request.getToAccount());
        creditLeg.setTransactionCode(request.getTransactionCode());
        creditLeg.setCurrency(request.getDestinationCurrency());
        creditLeg.setChargeBearer(request.getChargeBearer());
        String additionalField=StringUtils.isNotBlank(request.getAdditionaField())?SPACE_CHAR+request.getAdditionaField():"";
        
        log.info("request.getDestinationCurrency() {}", request.getDestinationCurrency());
		if (StringUtils.isNotBlank(request.getFinalBene())) {
			if (INTERNATIONAL.equalsIgnoreCase(request.getTransferType())
					|| NON_AED_LOCAL.equalsIgnoreCase(request.getTransferType())) {
				creditLeg.setPaymentDetails(
						request.getPurposeDesc() + SPACE_CHAR + request.getFinalBene() + additionalField);
			}
			else {
				creditLeg.setPaymentDetails(PAYMENT_DETAIL_PREFIX + request.getPurposeDesc() + SPACE_CHAR
						+ request.getFinalBene() + additionalField);
			}
		} else {
			if (INTERNATIONAL.equalsIgnoreCase(request.getTransferType())
					|| NON_AED_LOCAL.equalsIgnoreCase(request.getTransferType())) {
				creditLeg.setPaymentDetails(request.getPurposeDesc() + additionalField);
			} else {
				creditLeg.setPaymentDetails(PAYMENT_DETAIL_PREFIX + request.getPurposeDesc() + additionalField);
			}
		}
        creditLeg.setBenName(request.getBeneficiaryFullName());
        creditLeg.setAWInstName(request.getAwInstName());
        creditLeg.setAWInstBICCode(request.getAwInstBICCode());
        creditLeg.setBenAddr1(request.getBeneficiaryAddressOne());
        creditLeg.setBenAddr2(request.getBeneficiaryAddressTwo());
        creditLeg.setBenAddr3(request.getBeneficiaryAddressThree());
        //creditLeg.setAWInst1(request.getAcwthInst1());
        //creditLeg.setAWInst2(request.getAcwthInst2());
        

        if(request.getAmount() == null) {
            debitLeg.setAmount(request.getSrcAmount());
        }
		else {
			if (StringUtils.isNotBlank(request.getTxnCurrency())
					&& request.getTxnCurrency().equalsIgnoreCase(request.getSourceCurrency())) {
			    if( StringUtils.isNotBlank(request.getBeneficiaryAddressTwo()) && request.getBeneficiaryAddressTwo().equalsIgnoreCase(UAE_COUNTRY))
                    creditLeg.setAmount(request.getAmount());
			    else
                    debitLeg.setAmount(request.getAmount());
			} else {
				creditLeg.setAmount(request.getAmount());
			}
		}
        if(isInvestment(request)){
            debitLeg.setNarration1(generateNarrationForInvestment(request.getChannel(),request.getExchangeRate()));
        }

        FundTransferReqType.Transfer transfer = new FundTransferReqType.Transfer();
        transfer.setCreditLeg(creditLeg);
        transfer.setDebitLeg(debitLeg);
        transferList.add(transfer);
        if (StringUtils.isNotEmpty(request.getDealNumber()) && StringUtils.isNotEmpty(request.getDealRate().toPlainString())) {
        	fundTransferReqType.setDealReferenceNo(request.getDealNumber());
        	fundTransferReqType.setDealFlag(YesNo.Y.name());
            transfer.setDealDate(LocalDate.now().toString());
            transfer.setRate(request.getDealRate().toPlainString());
        } else {
        	fundTransferReqType.setDealFlag(YesNo.N.name());
        }
        services.getBody().setFundTransferReq(fundTransferReqType);
        log.info("EAI Service request for fund transfer prepared {}", services);
        return services;
    }

    private boolean isInvestment(FundTransferRequest request) {
               return (GOLD.equalsIgnoreCase(request.getDestinationCurrency())||
        GOLD.equalsIgnoreCase(request.getSourceCurrency())||
                       SILVER.equalsIgnoreCase(request.getDestinationCurrency())||
                       SILVER.equalsIgnoreCase(request.getSourceCurrency()));
    }

    private String generateNarration(String channel) {
        return NARRATION_PREFIX + channel + NARRATION_SUFFIX;
    }

    private String generateNarrationForInvestment(String channel, BigDecimal exchangeRate) {
        return NARRATION_PREFIX + channel + NARRATION_SUFFIX + " ExchangeRate " + exchangeRate;
    }


}
