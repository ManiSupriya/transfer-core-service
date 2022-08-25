package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferCCReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferCCResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfercc.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.config.FTCCConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapClient;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.mashreq.transfercoreservice.middleware.SoapWebserviceClientFactory.soapClient;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;


/**
 * This class is used to call the middle ware to process the Credit cards Fund Transfer request
 *
 * @author ThanigachalamP
 */
@Slf4j
@Service
@AllArgsConstructor
public class FundTransferCCMWService {


    public static final String HYPEN_DELIMITER = "-";
    public static final String FUND_TRANSFER_USING_CC = "Fund Transfer using CC";
    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private final FTCCConfig ftCCConfig;
    private final AsyncUserEventPublisher auditEventPublisher;
    private static final String SUCCESS = "S";
    private static final String FAILED = "Failed";
    private static final String SUCCESS_EXPAND = "SUCCESS";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private static final String PAYMENT_DETAIL_PREFIX = "/REF/ ";
    public static final String SPACE_CHAR = " ";
    
    
	private static final String AED_CURRENCY = "AED";


    public static final String DEBIT_ACCOUNT_BRANCH = "030";
    public static final String MONEY_TRANSFER = "Money transfer";

    /**
     * used to call the middle ware to process the Credit cards Fund Transfer request
     * @param fundTransferRequest
     * @param requestMetaData
     * @return
     */
    public FundTransferResponse transfer(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData) {
        log.info("Fund transfer CC initiated from account [ {} ]", htmlEscape(fundTransferRequest.getFromAccount()));
        String mwSrcMsgId = null;
        SoapClient mobSoapClient;
        EAIServices soapResponse;
        EAIServices.Body body;
        FundTransferCCResType fundTransferCCRes;
        ErrorType exceptionDetails;
        CoreFundTransferResponseDto coreFundTransferResponseDto;
        String remarks = null;
        MwResponseStatus mwResponseStatus;
        try{
            mwSrcMsgId = "FTCC"+System.currentTimeMillis()/1000;
            remarks = getRemarks(fundTransferRequest);
            mobSoapClient = soapClient(soapServiceProperties, new Class[]{
                    HeaderType.class,
                    EAIServices.class,
                    FundTransferCCReqType.class,
                    FundTransferCCResType.class,
                    ErrorType.class,
            });
            EAIServices eaiServices = generateEAIRequest(fundTransferRequest, requestMetaData, mwSrcMsgId);
            soapResponse = (EAIServices) mobSoapClient.exchange(eaiServices);
            if (soapResponse == null || soapResponse.getBody() == null) {
                logPublishFailureEvent(requestMetaData, FundTransferEventType.FUND_TRANSFER_CC_MW_CALL, TransferErrorCode.FT_CC_MW_EMPTY_RESPONSE,
                        null, mwSrcMsgId, remarks);
            }
            body = soapResponse.getBody();
            fundTransferCCRes = body.getFundTransferCCRes();
            exceptionDetails = body.getExceptionDetails();
            if (isSuccess(soapResponse)) {
                mwResponseStatus = MwResponseStatus.S;
                coreFundTransferResponseDto = constructFTResponseDTO(fundTransferCCRes, exceptionDetails, mwResponseStatus);
                auditEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.FUND_TRANSFER_CC_MW_CALL, requestMetaData, remarks, mwSrcMsgId);
                return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).transactionRefNo(coreFundTransferResponseDto.getHostRefNo()).build();
             } else {
                mwResponseStatus = MwResponseStatus.F;
                coreFundTransferResponseDto = constructFTResponseDTO(fundTransferCCRes, exceptionDetails, mwResponseStatus);
                auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.FUND_TRANSFER_CC_MW_CALL, requestMetaData, remarks, mwSrcMsgId,
                        coreFundTransferResponseDto.getMwResponseCode(), coreFundTransferResponseDto.getMwResponseDescription(), coreFundTransferResponseDto.getExternalErrorMessage());
                return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
            }
        }catch (Exception exception){
                logPublishFailureEvent(requestMetaData, FundTransferEventType.FUND_TRANSFER_CC_MW_CALL, TransferErrorCode.FT_CC_MW_ERROR, exception, mwSrcMsgId, remarks);
        }
        return null;
    }

    /**
     * Used to validate the middle ware response whether it is a success or failure
     * @param response
     * @return
     */
   private boolean isSuccess(EAIServices response) {
        log.info("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.error("FT CC Exception during fund transfer. Code: {} , Description: {}", response.getBody()
                    .getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getData());
            return false;
        }
        return true;
    }

    // TODO need to verify
    private CoreFundTransferResponseDto constructFTResponseDTO(FundTransferCCResType fundTransferCCResType, ErrorType exceptionDetails, MwResponseStatus s) {
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        String cardStatus;
        String coreStatus;
        TransferErrorCode transferErrorCode;
        if(fundTransferCCResType != null){
            coreFundTransferResponseDto.setHostRefNo(fundTransferCCResType.getCoreReferenceNumber());
            coreFundTransferResponseDto.setMwReferenceNo(fundTransferCCResType.getCardReferenceNumber());
            coreFundTransferResponseDto.setTransactionRefNo(fundTransferCCResType.getCardReferenceNumber());
        }
        if(exceptionDetails != null){
            coreFundTransferResponseDto.setExternalErrorMessage(exceptionDetails.getData());
            coreFundTransferResponseDto.setMwResponseDescription(exceptionDetails.getErrorDescription());
            coreFundTransferResponseDto.setMwResponseCode(exceptionDetails.getErrorCode());
        }
        coreFundTransferResponseDto.setMwResponseStatus(s);
        //Below code is changed to show specific error code in case if card status = SUCCESS and core status = Failed - 33462
        if(s.equals(MwResponseStatus.F)){
            cardStatus = fundTransferCCResType.getCardStatus();
            coreStatus = fundTransferCCResType.getCoreStatus();
            if(SUCCESS_EXPAND.equalsIgnoreCase(cardStatus) && FAILED.equalsIgnoreCase(coreStatus)){
                transferErrorCode = TransferErrorCode.FT_CC_MW_SUCCESS_FAILED_RESPONSE;
                coreFundTransferResponseDto.setMwResponseDescription(transferErrorCode.getErrorMessage());
                coreFundTransferResponseDto.setMwResponseCode(transferErrorCode.getCustomErrorCode());
            }
        }
        return coreFundTransferResponseDto;
    }

    /**
     * Used to build the request model for the middle ware
     * @param fundTransferRequest
     * @param requestMetaData
     * @param mwSrcMsgId
     * @return
     */
    public EAIServices generateEAIRequest(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData, String mwSrcMsgId) {

        EAIServices services = new EAIServices();
        services.setHeader(headerFactory.getHeader(ftCCConfig.getSrvCode(), mwSrcMsgId));
        services.setBody(new EAIServices.Body());
        FundTransferCCReqType fundTransferReqType = new FundTransferCCReqType();
        FundTransferCCReqType.CreditLeg creditLeg = new FundTransferCCReqType.CreditLeg();
        FundTransferCCReqType.DebitLeg debitLeg = new FundTransferCCReqType.DebitLeg();
        buildDebitLeg(fundTransferRequest, requestMetaData, debitLeg);
        buildCreditLeg(fundTransferRequest, mwSrcMsgId, creditLeg, requestMetaData);
        fundTransferReqType.setDebitLeg(debitLeg);
        fundTransferReqType.setCreditLeg(creditLeg);
        services.getBody().setFundTransferCCReq(fundTransferReqType);
        log.info("EAI Service request for fund transfer prepared {}", htmlEscape(services));
        return services;
    }

    /**
     * Used to build the credit leg as part of middle ware request
     * @param fundTransferRequest
     * @param mwSrcMsgId
     * @param creditLeg
     * @param requestMetaData
     */
    private void buildCreditLeg(FundTransferRequest fundTransferRequest, String mwSrcMsgId, FundTransferCCReqType.CreditLeg creditLeg, RequestMetaData requestMetaData) {
        FundTransferCCReqType.CreditLeg.SettlementAddlDetails settlementAddlDetails =
                new FundTransferCCReqType.CreditLeg.SettlementAddlDetails();
        FundTransferCCReqType.CreditLeg.SettlementAddlDetails.SettlementAddlMain settlementAddlMain =
                new FundTransferCCReqType.CreditLeg.SettlementAddlDetails.SettlementAddlMain();

        creditLeg.setExternalReferenceNo(mwSrcMsgId);
        creditLeg.setProductCode(ftCCConfig.getProductCode());
        creditLeg.setExchangeRate(convertToString(fundTransferRequest.getExchangeRate()));
        creditLeg.setRemarks(FUND_TRANSFER_USING_CC);
        creditLeg.setDebitAccountNo(ftCCConfig.getDebitAccountNo());
        creditLeg.setDebitAccountBranch(DEBIT_ACCOUNT_BRANCH);
        creditLeg.setDebitAmount(convertToString(fundTransferRequest.getAmount()));
        creditLeg.setDebitCurrency(fundTransferRequest.getSourceCurrency());
        creditLeg.setCreditAmount(convertToString(fundTransferRequest.getAmount()));
        creditLeg.setCreditCurrency(fundTransferRequest.getDestinationCurrency());
        creditLeg.setAuthStatus(ftCCConfig.getAuthStatus());
        creditLeg.setUltimateBeneficiary1(fundTransferRequest.getBeneficiaryFullName());
        creditLeg.setUltimateBeneficiary2(fundTransferRequest.getBeneficiaryFullName());
        creditLeg.setUltimateBeneficiary4(fundTransferRequest.getBeneficiaryFullName());
        creditLeg.setChargeBearer(fundTransferRequest.getChargeBearer());
        
        log.info("fundTransferRequest.getDestinationCurrency() {}", htmlEscape(fundTransferRequest.getDestinationCurrency()));
		if (StringUtils.isNotBlank(fundTransferRequest.getAcwthInst1())) {
			if (AED_CURRENCY.equalsIgnoreCase(fundTransferRequest.getDestinationCurrency())) {
				creditLeg.setPaymentDetails1(PAYMENT_DETAIL_PREFIX + fundTransferRequest.getPurposeDesc() + SPACE_CHAR
						+ fundTransferRequest.getAcwthInst1());
			} else {
				creditLeg.setPaymentDetails1(
						fundTransferRequest.getPurposeDesc() + SPACE_CHAR + fundTransferRequest.getAcwthInst1());
			}
		} else {
			if (AED_CURRENCY.equalsIgnoreCase(fundTransferRequest.getDestinationCurrency())) {
				creditLeg.setPaymentDetails1(PAYMENT_DETAIL_PREFIX + fundTransferRequest.getPurposeDesc());
			} else {
				creditLeg.setPaymentDetails1(fundTransferRequest.getPurposeDesc());
			}
		}
        creditLeg.setAcwthInst1(fundTransferRequest.getAcwthInst1());
        creditLeg.setAcwthInst2(fundTransferRequest.getAcwthInst2());
        creditLeg.setAcwthInst5(fundTransferRequest.getAcwthInst5());
        creditLeg.setMisDetails(null);

        settlementAddlDetails.setAmountTag(ftCCConfig.getAmountTag());
        settlementAddlMain.setOrderingCustomerAddress1(fundTransferRequest.getToAccount());
        settlementAddlMain.setOrderingCustomerAddress2(requestMetaData.getPrimaryCif());
        settlementAddlMain.setOrderingCustomerAddress3(fundTransferRequest.getBeneficiaryAddressTwo());
        settlementAddlMain.setOrderingCustomerAddress4(fundTransferRequest.getBeneficiaryAddressTwo());
        settlementAddlMain.setOrderingCustomerAddress5(fundTransferRequest.getBeneficiaryAddressTwo());
        settlementAddlMain.setMessageThrough(ftCCConfig.getMessageThrough());
        settlementAddlMain.setAmountTag(ftCCConfig.getAmountTag());
        settlementAddlMain.setTransTypeCode(ftCCConfig.getTransTypeCode());
        settlementAddlDetails.setSettlementAddlMain(settlementAddlMain);
        creditLeg.setSettlementAddlDetails(settlementAddlDetails);
    }

    /**
     * Used to build the debit leg as part of middle ware request
     * @param fundTransferRequest
     * @param requestMetaData
     * @param debitLeg
     */
    private void buildDebitLeg(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData, FundTransferCCReqType.DebitLeg debitLeg) {
        debitLeg.setCardNumber(fundTransferRequest.getCardNo());
        debitLeg.setBankRef(ftCCConfig.getBankReferenceNo());
        debitLeg.setAmountSRCCurrency(convertToString(fundTransferRequest.getAmount()));
        debitLeg.setSRCISOCurrency(fundTransferRequest.getSourceISOCurrency());
        debitLeg.setAmountDESTCurrency(convertToString(fundTransferRequest.getAmount()));
        debitLeg.setDESTISOCurrency(fundTransferRequest.getDestinationISOCurrency());
        updateExpiryDetails(fundTransferRequest, debitLeg);
        debitLeg.setMerchantId(ftCCConfig.getMerchantId());
        debitLeg.setTerminalId(ftCCConfig.getTerminalId());
        debitLeg.setCIFNo(requestMetaData.getPrimaryCif());
        debitLeg.setLocation(MONEY_TRANSFER);
    }

    /**
     * Utility which is used to convert from Big decimal to String
     * @param bigDecimal
     * @return
     */
    private String convertToString(BigDecimal bigDecimal){
        String value = null;
        if(bigDecimal != null){
            value = bigDecimal.toString();
        }
        return value;
    }

    /**
     * Used to update the expiry month and year from the card expiry date
     * @param fundTransferRequest
     * @param debitLeg
     */
    private void updateExpiryDetails(FundTransferRequest fundTransferRequest, FundTransferCCReqType.DebitLeg debitLeg){
        String expiryDate = fundTransferRequest.getExpiryDate();
        String[] splitValues;
        if(expiryDate != null && expiryDate.trim().length() > 0){
            splitValues = expiryDate.split(HYPEN_DELIMITER);
            if(splitValues.length == 3){
                debitLeg.setExpiryYear(splitValues[0]);
                debitLeg.setExpiryMonth(splitValues[1]);
            }
        }
    }

    /**
     * Log the failure event and throws an exception with proper error code
     * @param requestMetaData
     * @param auditEventType
     * @param errorCodeSet
     * @param exception
     * @param mwSrcMsgId
     * @param remarks
     */
    private void logPublishFailureEvent(RequestMetaData requestMetaData , FundTransferEventType auditEventType,
                                        TransferErrorCode errorCodeSet, Exception exception, String mwSrcMsgId, String remarks){
        auditEventPublisher.publishFailedEsbEvent(auditEventType, requestMetaData, remarks,mwSrcMsgId,
                errorCodeSet.name(), errorCodeSet.getErrorMessage(), errorCodeSet.getErrorMessage());
        if(exception == null){
            GenericExceptionHandler.handleError(errorCodeSet,errorCodeSet.getErrorMessage());
        }
        GenericExceptionHandler.handleError(errorCodeSet,errorCodeSet.getErrorMessage(), exception);
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

}
