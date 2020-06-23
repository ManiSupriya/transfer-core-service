package com.mashreq.transfercoreservice.client.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;

/**
 * @author shahbazkh
 * @date 4/1/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryService {

    public static final String TRUE = "true";
    private final BeneficiaryClient beneficiaryClient;

    public BeneficiaryDto getById(final String cifId, final Long id) {
        log.info("Fetching Beneficiary for id = {}", id);
        Response<BeneficiaryDto> response = beneficiaryClient.getById(cifId, id);

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            GenericExceptionHandler.handleError(BENE_EXTERNAL_SERVICE_ERROR, BENE_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    getErrorDetails(response));
        }
        log.info("Beneficiary fetched successfully for id = {}", id);
        return response.getData();
    }

}
