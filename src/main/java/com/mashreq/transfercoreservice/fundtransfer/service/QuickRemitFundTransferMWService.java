package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.customer.mbcdm.RemittancePaymentReqType;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.remittancepayment.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.QuickRemitResponseHandler;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuickRemitFundTransferMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;

    public FundTransferResponse transfer(QuickRemitFundTransferRequest request) {
        log.info("Quick remit fund transfer initiated from account [ {} ]", request.getSenderBankAccount());

        EAIServices response = (EAIServices) webServiceClient.exchange(generateEAIServiceRequest(request));

        final String transactionRefNo = response.getBody().getRemittancePaymentRes().getCoreReferenceNo();
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();

        MwResponseStatus mwResponseStatus = QuickRemitResponseHandler.responseHandler(response);

        final CoreFundTransferResponseDto coreFundTransferResponseDto = constructQRFTResponseDTO(transactionRefNo, request.getFinTxnNo(), exceptionDetails, mwResponseStatus);
        return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
    }

    private CoreFundTransferResponseDto constructQRFTResponseDTO(String transfer, String txnRefNum, ErrorType exceptionDetails, MwResponseStatus s) {
        return CoreFundTransferResponseDto.builder()
                .externalErrorMessage(exceptionDetails.getData())
                .mwReferenceNo(transfer)
                .mwResponseDescription(exceptionDetails.getErrorDescription())
                .mwResponseStatus(s)
                .mwResponseCode(exceptionDetails.getErrorCode())
                .transactionRefNo(txnRefNum)
                .build();
    }

    public EAIServices generateEAIServiceRequest(QuickRemitFundTransferRequest request) {

        EAIServices services = new EAIServices();
        services.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFundTransfer(), request.getChannelTraceId()));
        services.setBody(new EAIServices.Body());

        //Setting individual components
        RemittancePaymentReqType fundTransferReqType = new RemittancePaymentReqType();
        fundTransferReqType.setPaymentID(request.getFinTxnNo());
        fundTransferReqType.setOriginatingCountry(request.getOriginatingCountry());
        fundTransferReqType.setDestCountry(request.getDestCountry());
        fundTransferReqType.setBName(request.getBeneficiaryName());
        fundTransferReqType.setBFullName(request.getBeneficiaryFullName());
        fundTransferReqType.setBAddress(request.getBeneficiaryAddress());
        fundTransferReqType.setBCountry(request.getBeneficiaryCountry());
        fundTransferReqType.setBAccountNo(request.getBeneficiaryAccountNo());
        fundTransferReqType.setBBankName(request.getBeneficiaryBankName());
        fundTransferReqType.setBBankIFSC(request.getBeneficiaryBankIFSC());
        fundTransferReqType.setAmountSRCCurrency(request.getAmountSRCCurrency());
        fundTransferReqType.setAmountDESTCurrency(request.getAmountDESTCurrency());
        fundTransferReqType.setSRCCurrency(request.getSrcCurrency());
        fundTransferReqType.setDESTCurrency(request.getDestCurrency());
        fundTransferReqType.setSRCISOCurrency(request.getSrcISOCurrency());
        fundTransferReqType.setDESTISOCurrency(request.getDestISOCurrency());
        fundTransferReqType.setTransactionAmount(request.getTransactionAmount());
        fundTransferReqType.setTransactionCurrency(request.getTransactionCurrency());
        fundTransferReqType.setExchangeRate(request.getExchangeRate());
        fundTransferReqType.setReasonCode(request.getReasonCode());
        fundTransferReqType.setReasonText(request.getReasonText());
        fundTransferReqType.setSenderName(request.getSenderName());
        fundTransferReqType.setSenderMobileNo(request.getSenderMobileNo());
        fundTransferReqType.setSenderBankBranch(request.getSenderBankBranch());
        fundTransferReqType.setSenderBankAccount(request.getSenderBankAccount());
        fundTransferReqType.setSenderAddress(request.getSenderAddress());
        fundTransferReqType.setSenderCountryISOCode(request.getSenderCountryISOCode());
        fundTransferReqType.setSenderIDType(request.getSenderIDType());
        fundTransferReqType.setSenderIDNumber(request.getSenderIDNumber());

        services.getBody().setRemittancePaymentReq(fundTransferReqType);
        log.info("EAI Service request for quick remit fund transfer prepared {}", services);
        return services;
    }


}
