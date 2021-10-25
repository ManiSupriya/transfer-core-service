package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BankChargesServiceClient;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.AdditionalFields;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_CHARGES_EXTERNAL_SERVICE_ERROR;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankChargesService {

    public static final String TRUE = "true";
    private final BankChargesServiceClient bankChargesServiceClient;

    public TransactionChargesDto getTransactionCharges(final String accountClass, final String transactionCurrency, RequestMetaData metaData) {
        log.info("Fetching TransactionCharges for accountClass = {} and transaction currency = {}", accountClass,transactionCurrency);
        Response<TransactionChargesDto> response = bankChargesServiceClient.getTransactionCharges(accountClass,transactionCurrency);

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            GenericExceptionHandler.handleError(BANK_CHARGES_EXTERNAL_SERVICE_ERROR, BANK_CHARGES_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    getErrorDetails(response));
        }
        log.info("Fetched TransactionCharges successfully for accountClass = {} and transaction currency = {}", accountClass,transactionCurrency);
        return response.getData();
    }

    public static String getErrorDetails(Response response) {
        if (StringUtils.isNotBlank(response.getErrorDetails())) {
            return response.getErrorCode() + "," + response.getErrorDetails() + "," + response.getMessage();
        }
        return response.getErrorCode() + "," + response.getMessage();
    }

}
