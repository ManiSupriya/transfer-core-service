package com.mashreq.transfercoreservice.client.service;


import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.CustomerClient;
import com.mashreq.transfercoreservice.client.ErrorUtils;
import com.mashreq.transfercoreservice.client.dto.CustomerDetailsDto;


import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    public static final String TRUE = "true";
    private final CustomerClient customerClient;

    public CustomerDetailsDto getCustomerDetails(final String cif) {
        log.info("[CustomerService] calling customer service client for getting customer details");

        Response<CustomerDetailsDto> response = customerClient.getCustomerProfile(cif);
        if (TRUE.equalsIgnoreCase(response.getHasError()) || StringUtils.isNotBlank(response.getErrorCode())) {
            log.error("Error while calling Customer Service {} {} ",response.getErrorCode(),response.getErrorMessage());
            GenericExceptionHandler.handleError(TransferErrorCode.EXTERNAL_SERVICE_ERROR, TransferErrorCode.EXTERNAL_SERVICE_ERROR.getErrorMessage(), ErrorUtils.getErrorDetails(response));
        }
        return customerClient.getCustomerProfile(cif).getData();

    }
}
