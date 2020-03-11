package com.mashreq.transfercoreservice.soap.transfer;

import brave.Tracer;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.transfercoreservice.enums.CommonConstants;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.enums.YesNo;
import com.mashreq.transfercoreservice.soap.HeaderFactory;
import com.mashreq.transfercoreservice.soap.WebServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;

import java.security.SecureRandom;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MashreqFundTransferMWService {

    private final HeaderFactory headerFactory;
    private final WebServiceClient webServiceClient;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private Tracer tracer;

    public MashreqFundTransferMWResponse transferFund(MashreqFundTransferMWRequest mashreqTransferMWRequest) {

        MwResponseStatus mwResponseStatus = MwResponseStatus.F;
        EAIServices response = (EAIServices) webServiceClient.exchange(this.generateRequest(mashreqTransferMWRequest));

        String transactionRefNo = null;

        if(isResponseValid(response)){
            transactionRefNo = response.getBody().getFundTransferRes().getTransfer().get(0).getTransactionRefNo();
            mwResponseStatus = MwResponseStatus.S;
        }

        ErrorType errorType = response.getBody().getExceptionDetails();
        return MashreqFundTransferMWResponse.builder()
                .mwResponseStatus(mwResponseStatus)
                .billRefNo(transactionRefNo)
                .mwResponseCode(errorType.getErrorCode())
                .mwResponseDescription(errorType.getErrorDescription())
                .mwReferenceNo(errorType.getReferenceNo())
                .build();
    }

    private boolean isResponseValid(EAIServices response) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(),SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            return false;
        }
        return true;
    }

    private EAIServices generateRequest(MashreqFundTransferMWRequest mashreqTransferMWRequest) {

        EAIServices request = new EAIServices();
        request.setBody(new EAIServices.Body());

        FundTransferReqType transferReqType = new FundTransferReqType();
        FundTransferReqType.Transfer transfer = new FundTransferReqType.Transfer();
        SecureRandom secureRandom = new SecureRandom();

        int transactionId = (int) (secureRandom.nextInt() * 9000) + 1000;//TODO
        transferReqType.setBatchTransactionId(String.valueOf(transactionId));

        FundTransferReqType.Transfer.DebitLeg debitLeg = new FundTransferReqType.Transfer.DebitLeg();
        debitLeg.setDebitRefNo(tracer.currentSpan().context().traceIdString());
        debitLeg.setAccountNo(mashreqTransferMWRequest.getFromAccount());
        debitLeg.setTransferBranch(mashreqTransferMWRequest.getFromAccountBranch());
        debitLeg.setCurrency(mashreqTransferMWRequest.getFromAccountCurrency());
        debitLeg.setNarration1(CommonConstants.FundTransfer.FUND_TRANSFER);//TODO
        debitLeg.setInternalAccFlag("N"); //TODO

        FundTransferReqType.Transfer.CreditLeg creditLeg = new FundTransferReqType.Transfer.CreditLeg();
        creditLeg.setAccountNo(mashreqTransferMWRequest.getToAccount());
        creditLeg.setTransactionCode("096"); //TODO
        creditLeg.setNarration1(CommonConstants.FundTransfer.FUND_TRANSFER);//TODO
        creditLeg.setPaymentDetails(CommonConstants.FundTransfer.FUND_TRANSFER);//TODO

        if (mashreqTransferMWRequest.getCurrency().equalsIgnoreCase(mashreqTransferMWRequest.getFromAccountCurrency())) {
            debitLeg.setAmount(mashreqTransferMWRequest.getAmount());
        } else {
            creditLeg.setAmount(mashreqTransferMWRequest.getAmount());
        }
        transfer.setDebitLeg(debitLeg);
        transfer.setCreditLeg(creditLeg);
        transferReqType.getTransfer().add(transfer);

        if(StringUtils.isNotEmpty(mashreqTransferMWRequest.getDealNumber())){
            transferReqType.setDealReferenceNo(mashreqTransferMWRequest.getDealNumber());
            transferReqType.setDealFlag(YesNo.Y.name());
            transfer.setDealDate(LocalDate.now().toString());
            transfer.setRate(mashreqTransferMWRequest.getExchangeRate().toPlainString());
        }else{
            transferReqType.setDealFlag(YesNo.N.name());
        }
        request.getBody().setFundTransferReq(transferReqType);
        request.setHeader(headerFactory.getHeader(CommonConstants.EsbSrv.FUND_TRANSFER,//TODO
                mashreqTransferMWRequest.getSrcMessageId()));
        return request;
    }
}
