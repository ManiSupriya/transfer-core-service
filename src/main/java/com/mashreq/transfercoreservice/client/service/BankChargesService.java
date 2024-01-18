package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_CHARGES_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;

import com.mashreq.mobcommons.services.http.RequestMetadataInterceptor;
import com.mashreq.transfercoreservice.client.RequestMetadataMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BankChargesServiceClient;
import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankChargesService {

    public static final String TRUE = "true";
    private final BankChargesServiceClient bankChargesServiceClient;

    public TransactionChargesDto getTransactionCharges(final String accountClass, final String transactionCurrency, RequestMetaData metaData) {
        log.info("Fetching TransactionCharges for accountClass = {} and transaction currency = {}", htmlEscape(accountClass),htmlEscape(transactionCurrency));
        Map<String,String> headerMap = RequestMetadataMapper.collectRequestMetadataAsMap(metaData);
        Response<TransactionChargesDto> response = bankChargesServiceClient.getTransactionCharges(headerMap, accountClass,transactionCurrency);

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            GenericExceptionHandler.handleError(BANK_CHARGES_EXTERNAL_SERVICE_ERROR, BANK_CHARGES_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    getErrorDetails(response));
        }
        log.info("Fetched TransactionCharges successfully for accountClass = {} and transaction currency = {}", htmlEscape(accountClass),htmlEscape(transactionCurrency));
        return response.getData();
    }

    public static String getErrorDetails(Response response) {
        if (StringUtils.isNotBlank(response.getErrorDetails())) {
            return response.getErrorCode() + "," + response.getErrorDetails() + "," + response.getMessage();
        }
        return response.getErrorCode() + "," + response.getMessage();
    }

}
