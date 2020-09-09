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
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.ErrorCodeSet;
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
import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@Service
@AllArgsConstructor
public class FundTransferCCMWService {


    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private final FTCCConfig ftCCConfig;
    private final AsyncUserEventPublisher auditEventPublisher;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private static final String PAYMENT_DETAIL_PREFIX = "/REF/ ";


    public static final String DEBIT_ACCOUNT_BRANCH = "030";

    public FundTransferResponse transfer(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData) {
        log.info("Fund transfer initiated from account [ {} ]", htmlEscape(fundTransferRequest.getFromAccount()));
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
                logPublishFailureEvent(requestMetaData, FundTransferEventType.FUND_TRANSFER_CC_MW_CALL, ErrorCodeSet.FTCC0002,
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
                logPublishFailureEvent(requestMetaData, FundTransferEventType.FUND_TRANSFER_CC_MW_CALL, ErrorCodeSet.FTCC0001, exception, mwSrcMsgId, remarks);
        }
        return null;
    }


    private void logPublishFailureEvent(RequestMetaData requestMetaData , FundTransferEventType auditEventType,
                                        ErrorCodeSet errorCodeSet, Exception exception, String mwSrcMsgId, String remarks){
        auditEventPublisher.publishFailedEsbEvent(auditEventType, requestMetaData, remarks,mwSrcMsgId,
                errorCodeSet.name(), errorCodeSet.getErrorDesc(), errorCodeSet.getErrorDesc());
        if(exception == null){
            GenericExceptionHandler.handleError(errorCodeSet,errorCodeSet.getErrorDesc());
        }
        GenericExceptionHandler.handleError(errorCodeSet,errorCodeSet.getErrorDesc(), exception);
    }


    private boolean isSuccess(EAIServices response) {
        log.info("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.error("Exception during fund transfer. Code: {} , Description: {}", response.getBody()
                    .getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getData());

            return false;
        }
        return true;
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

    // TODO need to verify
    private CoreFundTransferResponseDto constructFTResponseDTO(FundTransferCCResType fundTransferCCResType, ErrorType exceptionDetails, MwResponseStatus s) {
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        if(fundTransferCCResType != null){
            coreFundTransferResponseDto.setHostRefNo(fundTransferCCResType.getCardReferenceNumber());
            coreFundTransferResponseDto.setMwReferenceNo(fundTransferCCResType.getCardReferenceNumber());
            coreFundTransferResponseDto.setTransactionRefNo(fundTransferCCResType.getCardReferenceNumber());
        }
        if(exceptionDetails != null){
            coreFundTransferResponseDto.setExternalErrorMessage(exceptionDetails.getData());
            coreFundTransferResponseDto.setMwResponseDescription(exceptionDetails.getErrorDescription());
            coreFundTransferResponseDto.setMwResponseCode(exceptionDetails.getErrorCode());
        }
        coreFundTransferResponseDto.setMwResponseStatus(s);
        return coreFundTransferResponseDto;
    }

    public EAIServices generateEAIRequest(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData, String mwSrcMsgId) {

        EAIServices services = new EAIServices();

        services.setHeader(headerFactory.getHeader(ftCCConfig.getSrvCode(), mwSrcMsgId));
        services.setBody(new EAIServices.Body());

        //Setting individual components
        FundTransferCCReqType fundTransferReqType = new FundTransferCCReqType();

        FundTransferCCReqType.CreditLeg creditLeg = new FundTransferCCReqType.CreditLeg();
        FundTransferCCReqType.DebitLeg debitLeg = new FundTransferCCReqType.DebitLeg();
        FundTransferCCReqType.CreditLeg.SettlementAddlDetails settlementAddlDetails =
                new FundTransferCCReqType.CreditLeg.SettlementAddlDetails();
        FundTransferCCReqType.CreditLeg.SettlementAddlDetails.SettlementAddlMain settlementAddlMain =
                new FundTransferCCReqType.CreditLeg.SettlementAddlDetails.SettlementAddlMain();


        testDataForDebitLeg(debitLeg);
        testDataForCreditLeg(creditLeg);
        testDataForSettlementAddlMain(settlementAddlDetails);


        /*debitLeg.setCardNumber(fundTransferRequest.getCardNo());
        debitLeg.setBankRef("363c5a1abb");
        debitLeg.setAmountSRCCurrency("10.00");
        // call other service to get the exact currency
        debitLeg.setSRCISOCurrency(fundTransferRequest.getSourceISOCurrency());
        debitLeg.setAmountDESTCurrency(fundTransferRequest.getDestinationCurrency());
        debitLeg.setDESTISOCurrency(fundTransferRequest.getDestinationISOCurrency());
        debitLeg.setExpiryYear("2021");
        debitLeg.setExpiryMonth("11");
        debitLeg.setMerchantId(ftCCConfig.getMerchantId());
        debitLeg.setTerminalId(ftCCConfig.getTerminalId());
        debitLeg.setCIFNo(requestMetaData.getPrimaryCif());

        creditLeg.setExternalReferenceNo("363c5a1abb");
        creditLeg.setProductCode("PACC");
        creditLeg.setExchangeRate(convertToString(fundTransferRequest.getExchangeRate()));
        creditLeg.setRemarks("Local Transfer using CC");
        creditLeg.setDebitAccountNo(fundTransferRequest.getFromAccount());
        creditLeg.setDebitAccountBranch(DEBIT_ACCOUNT_BRANCH);
        creditLeg.setDebitAmount(convertToString(fundTransferRequest.getAmount()));
        creditLeg.setDebitCurrency(fundTransferRequest.getSourceCurrency());
        creditLeg.setDebitCurrency("AED");
        creditLeg.setCreditAmount(fundTransferRequest.getToAccount());
        creditLeg.setCreditCurrency(fundTransferRequest.getDestinationCurrency());
        creditLeg.setCreditCurrency("AED");
        creditLeg.setAuthStatus(ftCCConfig.getAuthStatus());
        creditLeg.setUltimateBeneficiary1(fundTransferRequest.getBeneficiaryFullName());
        creditLeg.setUltimateBeneficiary2(fundTransferRequest.getBeneficiaryFullName());
        creditLeg.setUltimateBeneficiary4(fundTransferRequest.getBeneficiaryFullName());
        creditLeg.setChargeBearer(fundTransferRequest.getChargeBearer());
        creditLeg.setPaymentDetails1(PAYMENT_DETAIL_PREFIX + fundTransferRequest.getPurposeDesc());
        creditLeg.setAcwthInst1(fundTransferRequest.getBeneficiaryAddressOne());
        creditLeg.setAcwthInst1("EBILAEADXXX");
        creditLeg.setAcwthInst2(fundTransferRequest.getBeneficiaryAddressTwo());
        creditLeg.setAcwthInst5(ftCCConfig.getAcwthInst5());
        creditLeg.setMisDetails(null);
        settlementAddlDetails.setAmountTag(ftCCConfig.getAmountTag());
        settlementAddlMain.setOrderingCustomerAddress1(fundTransferRequest.getBeneficiaryAddressOne());
        settlementAddlMain.setOrderingCustomerAddress2(fundTransferRequest.getBeneficiaryAddressTwo());
        settlementAddlMain.setOrderingCustomerAddress3(fundTransferRequest.getBeneficiaryAddressThree());
        settlementAddlMain.setOrderingCustomerAddress4("TEST_CUSTOMER_010626928");
        settlementAddlMain.setOrderingCustomerAddress5("TEST_CUSTOMER_010626928");
        settlementAddlMain.setMessageThrough(ftCCConfig.getMessageThrough());
        settlementAddlMain.setAmountTag(ftCCConfig.getAmountTag());
        settlementAddlMain.setTransTypeCode(ftCCConfig.getTransTypeCode());
        settlementAddlDetails.setSettlementAddlMain(settlementAddlMain);*/
        creditLeg.setSettlementAddlDetails(settlementAddlDetails);
        fundTransferReqType.setDebitLeg(debitLeg);
        fundTransferReqType.setCreditLeg(creditLeg);
        services.getBody().setFundTransferCCReq(fundTransferReqType);
        log.info("EAI Service request for fund transfer prepared {}", services);
        return services;
    }

    private String convertToString(BigDecimal bigDecimal){
        String value = null;
        if(bigDecimal != null){
            value = bigDecimal.toString();
        }
        return value;
    }

    private void updateExpiryDetails(FundTransferRequest fundTransferRequest, FundTransferCCReqType.DebitLeg debitLeg){
        String expiryDate = fundTransferRequest.getExpiryDate();
        String[] splitValues;
        if(expiryDate != null && expiryDate.trim().length() > 0){
            splitValues = expiryDate.split("-");
            if(splitValues.length == 3){
                debitLeg.setExpiryYear(splitValues[0]);
                debitLeg.setExpiryMonth(splitValues[1]);
            }
        }
    }

    private void testDataForDebitLeg(FundTransferCCReqType.DebitLeg debitLeg){
        debitLeg.setCardNumber("524137******9908");
        debitLeg.setBankRef("363c5a1abb");
        debitLeg.setAmountSRCCurrency("10.00");
        // call other service to get the exact currency
        debitLeg.setSRCISOCurrency("784");
        debitLeg.setAmountDESTCurrency("10.00");
        debitLeg.setDESTISOCurrency("784");
        debitLeg.setExpiryYear("2021");
        debitLeg.setExpiryMonth("11");
        debitLeg.setMerchantId("000008026734");
        debitLeg.setTerminalId("20091124");
        debitLeg.setCIFNo("010626928");
    }

    private void testDataForCreditLeg(FundTransferCCReqType.CreditLeg creditLeg){
        creditLeg.setExternalReferenceNo("3237gf6666");
        creditLeg.setProductCode("PACC");
        creditLeg.setExchangeRate("");
        creditLeg.setRemarks("Local Transfer using CC");
        creditLeg.setDebitAccountNo("011099270455");
        creditLeg.setDebitAccountBranch(DEBIT_ACCOUNT_BRANCH);
        creditLeg.setDebitAmount("10.00");
        creditLeg.setDebitCurrency("AED");
        creditLeg.setCreditAmount("10.00");
        creditLeg.setCreditCurrency("AED");
        creditLeg.setAuthStatus("A");
        creditLeg.setUltimateBeneficiary1("AE140260001014232408901");
        creditLeg.setUltimateBeneficiary2("Test local");
        creditLeg.setUltimateBeneficiary4("UNITED ARAB EMIRATES");
        creditLeg.setChargeBearer("O");
        creditLeg.setPaymentDetails1("/REF/ Family Support");
        creditLeg.setAcwthInst1("EBILAEADXXX");
        creditLeg.setAcwthInst2("EMIRATESNBD BANK PJSC");
        creditLeg.setAcwthInst5("UNITED ARAB EMIRATES");
        creditLeg.setMisDetails(null);
    }

    private void testDataForSettlementAddlMain(FundTransferCCReqType.CreditLeg.SettlementAddlDetails settlementAddlDetails){
        FundTransferCCReqType.CreditLeg.SettlementAddlDetails.SettlementAddlMain settlementAddlMain =
                new FundTransferCCReqType.CreditLeg.SettlementAddlDetails.SettlementAddlMain();
        settlementAddlDetails.setAmountTag("TFR_AMT");
        settlementAddlMain.setOrderingCustomerAddress1("AE700330000011099270455");
        settlementAddlMain.setOrderingCustomerAddress2("010626928");
        settlementAddlMain.setOrderingCustomerAddress3("TEST_CUSTOMER_010626928");
        settlementAddlMain.setOrderingCustomerAddress4("TEST_CUSTOMER_010626928");
        settlementAddlMain.setOrderingCustomerAddress5("TEST_CUSTOMER_010626928");
        settlementAddlMain.setMessageThrough("U");
        settlementAddlMain.setAmountTag("TFR_AMT");
        settlementAddlMain.setTransTypeCode("FAM");
        settlementAddlDetails.setSettlementAddlMain(settlementAddlMain);
    }

}
