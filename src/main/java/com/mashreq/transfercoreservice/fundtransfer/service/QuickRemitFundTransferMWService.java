package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.customer.mbcdm.RemittancePaymentReqType;
import com.mashreq.esbcore.bindings.customer.mbcdm.RemittancePaymentReqType.RoutingCode;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.remittancepayment.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.QuickRemitResponseHandler;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuickRemitFundTransferMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;
    private final AsyncUserEventPublisher auditEventPublisher;

    public FundTransferResponse transfer(QuickRemitFundTransferRequest request, RequestMetaData metaData) {
        log.info("Quick remit fund transfer initiated from account [ {} ]", request.getSenderBankAccount());

        EAIServices response = (EAIServices) webServiceClient.exchange(generateEAIServiceRequest(request));

        final String transactionRefNo = response.getBody().getRemittancePaymentRes().getCoreReferenceNo();
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();

        MwResponseStatus mwResponseStatus = QuickRemitResponseHandler.responseHandler(response);

        final CoreFundTransferResponseDto coreFundTransferResponseDto = constructQRFTResponseDTO(transactionRefNo, request.getFinTxnNo(), exceptionDetails, mwResponseStatus);
        if(MwResponseStatus.F == mwResponseStatus) {
            auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.QR_FUND_TRANSFER_MW_CALL, metaData, getRemarks(request),request.getChannelTraceId(),
                    coreFundTransferResponseDto.getMwResponseCode(), coreFundTransferResponseDto.getMwResponseDescription(), coreFundTransferResponseDto.getExternalErrorMessage());
        }
        else {
            auditEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.QR_FUND_TRANSFER_MW_CALL, metaData, getRemarks(request),request.getChannelTraceId());
        }

        return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
    }

    private String getRemarks(QuickRemitFundTransferRequest request) {
        return String.format("From Account = %s, To Account = %s, Amount = %s, Transaction Currency = %s, Destination Currency = %s, Source Currency = %s," +
                        "Service code = %s, Financial Transaction Number = %s, Beneficiary full name = %s, Beneficiary IFSC= %s, Beneficiary routing code = %s," +
                        "Destination country = %s ",
                request.getSenderBankAccount(),
                request.getBeneficiaryAccountNo(),
                request.getTransactionAmount(),
                request.getDestCurrency(),
                request.getSrcCurrency(),
                request.getServiceCode(),
                request.getFinTxnNo(),
                request.getBeneficiaryFullName(),
                request.getBeneficiaryBankIFSC(),
                request.getRoutingCode(),
                request.getDestCountry()
        );
    }

    private CoreFundTransferResponseDto constructQRFTResponseDTO(String transfer, String txnRefNum, ErrorType exceptionDetails, MwResponseStatus s) {
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        	coreFundTransferResponseDto.setExternalErrorMessage(exceptionDetails.getData());
        	coreFundTransferResponseDto.setMwReferenceNo(transfer);
        	coreFundTransferResponseDto.setMwResponseDescription(exceptionDetails.getErrorDescription());
        	coreFundTransferResponseDto.setMwResponseStatus(s);
        	coreFundTransferResponseDto.setMwResponseCode(exceptionDetails.getErrorCode());
        	coreFundTransferResponseDto.setTransactionRefNo(txnRefNum);
                return coreFundTransferResponseDto;
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
        remittancePaymentReq.setRoutingCode(getRoutingCode(request));
        remittancePaymentReq.setPaymentNarration(request.getPaymentNarration());
        remittancePaymentReq.setBCity(request.getBeneficiaryCity());
        remittancePaymentReq.setBPinCode(request.getBeneficiaryPinCode());
        remittancePaymentReq.setBState(request.getBeneficiaryState());
        remittancePaymentReq.setSenderDOB(request.getSenderDOB());

        services.getBody().setRemittancePaymentReq(remittancePaymentReq);

        log.info("EAI Service request for quick remit fund transfer prepared {}", services);
        return services;
    }

    private List<RoutingCode> getRoutingCode(QuickRemitFundTransferRequest request) {
        return CollectionUtils.isNotEmpty(request.getRoutingCode())
                ? request.getRoutingCode().stream().map(routingCode -> mapRoutingCode(routingCode)).collect(Collectors.toList())
                : null;
    }

    private RoutingCode mapRoutingCode(com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode x) {
        RoutingCode routingCode = new RoutingCode();
        routingCode.setType(x.getType());
        routingCode.setValue(x.getValue());
        return routingCode;
    }


}
