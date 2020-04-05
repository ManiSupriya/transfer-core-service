package com.mashreq.transfercoreservice.client.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.ErrorUtils;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.config.feign.FeignResponse;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_NOT_FOUND;

/**
 * @author shahbazkh
 * @date 4/1/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryClient beneficiaryClient;

    public BeneficiaryDto getById(final String cifId, final Long id) {
        Response<BeneficiaryDto> response = beneficiaryClient.getById(cifId, id);
        FeignResponse<BeneficiaryDto> feignResponse = FeignResponse.class.cast(response);

        if (ErrorUtils.hasError(feignResponse)) {
            GenericExceptionHandler.handleError(
                    BENE_NOT_FOUND,
                    BENE_NOT_FOUND.getErrorMessage(),
                    ErrorUtils.getErrorDetails(feignResponse));
        }
        return feignResponse.getData();
    }

}
