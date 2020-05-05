package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.customer.mbcdm.RemittancePaymentReqType;
import com.mashreq.esbcore.bindings.customer.mbcdm.RemittancePaymentReqType.RoutingCode;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.remittancepayment.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.QuickRemitResponseHandler;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuickRemitFundTransferMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;

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
        services.setHeader(headerFactory.getHeader(request.getServiceCode(), request.getChannelTraceId()));
        services.setBody(new EAIServices.Body());

        //Setting individual components for IN and PK
        RemittancePaymentReqType remittancePaymentReq = new RemittancePaymentReqType();
        remittancePaymentReq.setPaymentID(request.getFinTxnNo());
        remittancePaymentReq.setOriginatingCountry(request.getOriginatingCountry());
        remittancePaymentReq.setDestCountry(request.getDestCountry());
        remittancePaymentReq.setBName(request.getBeneficiaryName());
        remittancePaymentReq.setBFullName(request.getBeneficiaryFullName());
        remittancePaymentReq.setBAddress(request.getBeneficiaryAddress());
        remittancePaymentReq.setBCountry(request.getBeneficiaryCountry());
        remittancePaymentReq.setBAccountNo(request.getBeneficiaryAccountNo());
        remittancePaymentReq.setBBankName(request.getBeneficiaryBankName());
        remittancePaymentReq.setBBankIFSC(request.getBeneficiaryBankIFSC());
        remittancePaymentReq.setAmountSRCCurrency(request.getAmountSRCCurrency());
        remittancePaymentReq.setAmountDESTCurrency(request.getAmountDESTCurrency());
        remittancePaymentReq.setSRCCurrency(request.getSrcCurrency());
        remittancePaymentReq.setDESTCurrency(request.getDestCurrency());
        remittancePaymentReq.setSRCISOCurrency(request.getSrcISOCurrency());
        remittancePaymentReq.setDESTISOCurrency(request.getDestISOCurrency());
        remittancePaymentReq.setTransactionAmount(request.getTransactionAmount());
        remittancePaymentReq.setTransactionCurrency(request.getTransactionCurrency());
        remittancePaymentReq.setExchangeRate(request.getExchangeRate());
        remittancePaymentReq.setReasonCode(request.getReasonCode());
        remittancePaymentReq.setReasonText(request.getReasonText());
        remittancePaymentReq.setSenderName(request.getSenderName());
        remittancePaymentReq.setSenderMobileNo(request.getSenderMobileNo());
        remittancePaymentReq.setSenderBankBranch(request.getSenderBankBranch());
        remittancePaymentReq.setSenderBankAccount(request.getSenderBankAccount());
        remittancePaymentReq.setSenderAddress(request.getSenderAddress());
        remittancePaymentReq.setSenderCountryISOCode(request.getSenderCountryISOCode());
        remittancePaymentReq.setSenderIDType(request.getSenderIDType());
        remittancePaymentReq.setSenderIDNumber(request.getSenderIDNumber());
        remittancePaymentReq.setBMobileNo(request.getBeneficiaryMobileNo());

        //For PK
        remittancePaymentReq.setBBankCode(request.getBeneficiaryBankCode());
        remittancePaymentReq.setBeneIDType(request.getBeneficiaryIdType());
        remittancePaymentReq.setBeneIDNo(request.getBeneficiaryIdNo());
        remittancePaymentReq.setDistributionType(request.getDistributionType());
        remittancePaymentReq.setTransferType(request.getTransferType());


        //Insta Rem
        remittancePaymentReq.setBAccountType(request.getBeneficiaryAccountType());
        remittancePaymentReq.setBBankAccountType(request.getBeneficiaryBankAccountType());
        remittancePaymentReq.setBEmail(request.getBeneficiaryEmail());
        //remittancePaymentReq.setSenderInitialAllowed();
        remittancePaymentReq.setSenderAccountType(request.getSenderAccountType());
        remittancePaymentReq.setSenderState(request.getSenderState());
        remittancePaymentReq.setSenderCity(request.getSenderCity());
        remittancePaymentReq.setSenderPostalCode(request.getSenderPostalCode());
        remittancePaymentReq.setSenderBeneficiaryRelationShip(request.getSenderBeneficiaryRelationship());
        remittancePaymentReq.setSenderSourceOfIncome(request.getSenderSourceOfIncome());
        remittancePaymentReq.setProductCode(request.getProductCode());
        remittancePaymentReq.setRoutingCode(
                request.getRoutingCode().stream().map(routingCode -> mapRoutingCode(routingCode)).collect(Collectors.toList())
        );
 
        services.getBody().setRemittancePaymentReq(remittancePaymentReq);

        log.info("EAI Service request for quick remit fund transfer prepared {}", services);
        return services;
    }

    private RoutingCode mapRoutingCode(com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode x) {
        RoutingCode routingCode = new RoutingCode();
        routingCode.setType(x.getType());
        routingCode.setValue(x.getValue());
        return routingCode;
    }


}
