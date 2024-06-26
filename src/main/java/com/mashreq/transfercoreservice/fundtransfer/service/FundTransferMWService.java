package com.mashreq.transfercoreservice.fundtransfer.service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;
import static com.mashreq.transfercoreservice.middleware.SoapWebserviceClientFactory.soapClient;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.mashreq.transfercoreservice.config.EscrowConfig;
import com.mashreq.transfercoreservice.fundtransfer.dto.ContractProjectDetails;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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


@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferMWService {

	private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;

    private final EscrowConfig escrowConfig;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private static final String NARRATION_PREFIX = "Fund Transfer-";
    private static final String NARRATION_SUFFIX = " Banking";
    private static final String PAYMENT_DETAIL_PREFIX = "/REF/ ";
    public static final String SPACE_CHAR = " ";
    private final AsyncUserEventPublisher auditEventPublisher;
    private static final String GOLD = "XAU";
    private static final String SILVER = "XAG";
    private static final String AACT = "AACT";
    private static final String UAE_COUNTRY= "UNITED ARAB EMIRATES";
    
    public static final String INTERNATIONAL = "INFT";
    public static final String LOCAL = "LOCAL";

    @Value("${app.local.currency}")
    private String localCurrency;

    @Value("${app.local.tftAuthorizationCode}")
    private String tftAuthorization;
    @Value("${app.inft.tftAuthorizationCode}")
    private String tftInftAuthorization;
    
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
        if (!isSuccessfull(response) && ObjectUtils.isEmpty(response.getBody().getFundTransferRes())) {
        	log.info("Fund transfer failed to account [ {} ]", htmlEscape(request.getToAccount()));
        	auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.FUND_TRANSFER_MW_CALL, metaData, getRemarks(request), msgId,
        			response.getBody().getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getErrorDescription(), response.getBody().getExceptionDetails().getErrorCode());
        	GenericExceptionHandler.handleError(TransferErrorCode.EXTERNAL_SERVICE_ERROR_MW,
					TransferErrorCode.EXTERNAL_SERVICE_ERROR_MW.getErrorMessage(),
					response.getBody().getExceptionDetails().getErrorCode()+"|"+response.getBody().getExceptionDetails().getErrorDescription());
        }
        final FundTransferResType.Transfer transfer = response.getBody().getFundTransferRes().getTransfer().get(0);
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();
        if (isSuccessfull(response)) {
            auditEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.FUND_TRANSFER_MW_CALL, metaData, getRemarks(request), msgId);
            log.info("Fund transferred successfully to account [ {} ]", htmlEscape(request.getToAccount()));
            final CoreFundTransferResponseDto coreFundTransferResponseDto = constructFTResponseDTO(transfer, exceptionDetails, MwResponseStatus.S);
            return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
        }

        log.info("Fund transfer failed to account [ {} ]", htmlEscape(request.getToAccount()));
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
        log.info("Validate response {}", htmlEscape(response));
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
        services.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFundTransfer(),request.getProductId() ,msgId));
        services.setBody(new EAIServices.Body());
        
        //Setting individual components
        FundTransferReqType fundTransferReqType = new FundTransferReqType();

        //TODO Change this to proper batch id
        fundTransferReqType.setBatchTransactionId(batchTransIdTemporary + "");
        fundTransferReqType.setPostingGroup(request.getPostingGroup());
        fundTransferReqType.setProductId(request.getProductId());
        fundTransferReqType.setTransTypeCode(request.getPurposeCode());
        /**
         * Hard-coding this values as per the request from Business 
         * All transactions for RETAIL customers are STP only irrespective of any txn currency
         * Later for SME this value should be calculated dynamically based on the input */

        /***
         * Changing it from configuration because in Egypt
         * 1- Local transfers are STP
         * 2- While INFT transfers are non STP.
         * By default value is still AUTHORIZED.
         */
        if(INTERNATIONAL.equalsIgnoreCase(request.getServiceType()))
            fundTransferReqType.setAuthorization(tftInftAuthorization);
        else
            fundTransferReqType.setAuthorization(tftAuthorization);

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
        
        /** only applicable for SWIFT transfers :: added as part of new DLS enhancement */
        creditLeg.setIntermBICCode(request.getIntermediaryBankSwiftCode());
        String additionalField = StringUtils.isNotBlank(request.getPaymentNote()) ? SPACE_CHAR + request.getPaymentNote() : "";
        
        log.info("request.getDestinationCurrency() {}", htmlEscape(request.getDestinationCurrency()));
		if (StringUtils.isNotBlank(request.getFinalBene())) {
			if (INTERNATIONAL.equalsIgnoreCase(request.getServiceType())
					|| (LOCAL.equalsIgnoreCase(request.getServiceType())
							&& !localCurrency.equalsIgnoreCase(request.getDestinationCurrency()))) {
				creditLeg.setPaymentDetails(
						request.getPurposeDesc() + SPACE_CHAR + request.getFinalBene()  + additionalField);
			}
			else {
                if(AACT.equalsIgnoreCase(request.getProductId())){
                    creditLeg.setPaymentDetails(StringUtils.isBlank(additionalField) ? PAYMENT_DETAIL_PREFIX : additionalField.trim());
                }else{
                    creditLeg.setPaymentDetails(PAYMENT_DETAIL_PREFIX + request.getPurposeDesc() + SPACE_CHAR
                            + request.getFinalBene()  + additionalField);
                }
			}
		} else {
			if (INTERNATIONAL.equalsIgnoreCase(request.getServiceType())
					|| (LOCAL.equalsIgnoreCase(request.getServiceType())
							&& !localCurrency.equalsIgnoreCase(request.getDestinationCurrency()))) {
				creditLeg.setPaymentDetails(request.getPurposeDesc()  + additionalField);
			} else {
                if(AACT.equalsIgnoreCase(request.getProductId())){
                    creditLeg.setPaymentDetails(StringUtils.isBlank(additionalField) ? PAYMENT_DETAIL_PREFIX : additionalField.trim());
                }else {
                    creditLeg.setPaymentDetails(PAYMENT_DETAIL_PREFIX + request.getPurposeDesc()  + additionalField);
                }
            }
		}
        creditLeg.setBenName(request.getBeneficiaryFullName());
        creditLeg.setAWInstName(request.getAwInstName());
        creditLeg.setAWInstBICCode(request.getAwInstBICCode());
        //creditLeg.setAWInstAddr1(value);
        //creditLeg.setAWInstAddr2(value);
       // creditLeg.setAWInstAddr3(value);
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
			    if( StringUtils.isNotBlank(request.getBeneficiaryBankCountry()) && request.getBeneficiaryBankCountry().equalsIgnoreCase(UAE_COUNTRY))
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
        //Escrow details For WAMA type
        if (isEscrowEnabled(request)) {
            buildEscrowDetails(request.getContractProjectDetails(), fundTransferReqType);
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
            fundTransferReqType.setAccountClass(request.getAccountClass());
        services.getBody().setFundTransferReq(fundTransferReqType);
        log.info("EAI Service request for fund transfer prepared {}", htmlEscape(services));
        return services;
    }

    private void buildEscrowDetails(ContractProjectDetails contractProjectDetails, FundTransferReqType fundTransferReqType) {
        FundTransferReqType.ContractPrjDtls projectDetails = new FundTransferReqType.ContractPrjDtls();
        projectDetails.setModule(contractProjectDetails.getModule());
        projectDetails.setProjectName(contractProjectDetails.getProjectName());
        projectDetails.setUnitId(contractProjectDetails.getUnitId());
        projectDetails.setUnitPayment(contractProjectDetails.getUnitPayment());
        projectDetails.setDepositTfrNo(contractProjectDetails.getDepositTfrNo());
        fundTransferReqType.setContractPrjDtls(projectDetails);
    }

    private boolean isEscrowEnabled(FundTransferRequest request) {
        return escrowConfig.isEnabled() && isWAMA(request.getServiceType()) && Objects.nonNull(request.getContractProjectDetails());
    }

    private boolean isWAMA(String serviceType) {
        return WAMA.getName().equals(serviceType);
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
