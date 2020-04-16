package com.mashreq.transfercoreservice.client.mobcommon;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobCommonService {

    private final MobCommonClient mobCommonClient;
    public static final String TRUE = "true";

    public LimitValidatorResultsDto validateAvailableLimit(String cifId, String beneficiaryTypeCode, BigDecimal amount) {

        log.info("[MobCommonService] Calling MobCommonService for limit validation for cif={} beneficiaryTypeCode = {} " +
                        "and amount ={}",
                cifId, beneficiaryTypeCode, amount);

        long startTime = System.nanoTime();
        Response<LimitValidatorResultsDto> limitValidatorResultsDtoResponse =
                mobCommonClient.validateAvailableLimit(cifId, beneficiaryTypeCode, amount);

        if (TRUE.equalsIgnoreCase(limitValidatorResultsDtoResponse.getHasError())) {
            final String errorDetails = getErrorDetails(limitValidatorResultsDtoResponse);
            log.error("[MobCommonService] Exception in calling mob customer for limit validation ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), errorDetails);
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        log.info("[MobCommonService] MobCommonService response success in nanoseconds {} ", totalTime);

        return limitValidatorResultsDtoResponse.getData();
    }
}
