package com.mashreq.transfercoreservice.middleware;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import com.mashreq.esbcore.bindings.account.mbcdm.AccountSummaryReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.fundtransfer.service.AccountDetailsEaiServices;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferEaiServices;
import com.mashreq.transfercoreservice.middleware.enums.YesNo;

import brave.Tracer;
import lombok.AllArgsConstructor;
@Component
@AllArgsConstructor
public class EsbRequestsCreator {

    private final HeaderFactory headerfactory;
    private Tracer tracer;
    private final SoapServiceProperties appProperties;
    
    /**
     * Create found transfer request
     */
    public FundTransferEaiServices createFundTransferRequest(String fromAccount, BigDecimal amount, String currency,
                                                             String destinationAccount, String fromAccountBranch,
                                                             String fromAccountCurrency,
                                                             String dealNumber,
                                                             CurrencyConversionDto currencyConversionDto) {

        FundTransferEaiServices eaiServices = new FundTransferEaiServices();
        eaiServices.setBody(new FundTransferEaiServices.Body());
        FundTransferReqType transferReqType = new FundTransferReqType();
        FundTransferReqType.Transfer transfer = new FundTransferReqType.Transfer();
        SecureRandom secureRandom = new SecureRandom();

        int transactionId = (int) (secureRandom.nextInt() * 9000) + 1000;
        transferReqType.setBatchTransactionId(String.valueOf(transactionId));

        FundTransferReqType.Transfer.DebitLeg debitLeg = new FundTransferReqType.Transfer.DebitLeg();
        debitLeg.setDebitRefNo(tracer.currentSpan().context().traceIdString());
        debitLeg.setAccountNo(fromAccount);
        debitLeg.setTransferBranch(fromAccountBranch);
        debitLeg.setCurrency(fromAccountCurrency);
        debitLeg.setNarration1(CommonConstants.FUND_TRANSFER);
        debitLeg.setInternalAccFlag("N");

        FundTransferReqType.Transfer.CreditLeg creditLeg = new FundTransferReqType.Transfer.CreditLeg();
        creditLeg.setAccountNo(destinationAccount);
        creditLeg.setTransactionCode("096");
        creditLeg.setNarration1(CommonConstants.FUND_TRANSFER);
        creditLeg.setPaymentDetails(CommonConstants.FUND_TRANSFER);

        if (currency.equalsIgnoreCase(fromAccountCurrency)) {
            debitLeg.setAmount(amount);
        } else {
            creditLeg.setAmount(amount);
        }
        transfer.setDebitLeg(debitLeg);
        transfer.setCreditLeg(creditLeg);
        transferReqType.getTransfer().add(transfer);

        if (StringUtils.isNotEmpty(dealNumber)) {
            transferReqType.setDealReferenceNo(dealNumber);
            transferReqType.setDealFlag(YesNo.Y.name());
            transfer.setDealDate(LocalDate.now().toString());
            transfer.setRate(currencyConversionDto.getExchangeRate().toPlainString());
        } else {
            transferReqType.setDealFlag(YesNo.N.name());
        }
        eaiServices.getBody().setFundTransferReq(transferReqType);
        eaiServices.setHeader(headerfactory.getHeader(appProperties.getServiceCodes().getFundTransfer(), tracer.currentSpan().context().traceIdString()));
        return eaiServices;
    }
    
    /**
     * Build request for acount details for ESB
     */
    public AccountDetailsEaiServices createEsbAccountDetaisRequest(String accNo) {
        AccountSummaryReqType reqType = new AccountSummaryReqType();
        reqType.setAccountNo(accNo);
        AccountDetailsEaiServices.Body body = new AccountDetailsEaiServices.Body();
        body.setAccountDetailsReq(reqType);
        AccountDetailsEaiServices request = new AccountDetailsEaiServices();
        request.setHeader(headerfactory.getHeader(appProperties.getServiceCodes().getSearchAccountDetails(), tracer.currentSpan().context().traceIdString()));
        request.setBody(body);
        return request;
    }

}
