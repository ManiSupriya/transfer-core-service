package com.mashreq.transfercoreservice.client.mobcommon;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobCommonService {

    private final MobCommonClient mobCommonClient;

    public LimitValidatorResultsDto validateAvailableLimit(String cifId, String beneficiaryTypeCode, BigDecimal amount){

        log.info("[MobCommonService] Calling MobCommonService for limit validation for cif={} beneficiaryTypeCode = {} and amount ={}",
                cifId, beneficiaryTypeCode, amount);

        try{

            long startTime = System.nanoTime();
            Response<LimitValidatorResultsDto> limitValidatorResultsDtoResponse =
                    mobCommonClient.validateAvailableLimit(cifId, beneficiaryTypeCode, amount);
            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            log.info("[MobCommonService] MobCommonService response success in nanoseconds {} ", totalTime);

            return limitValidatorResultsDtoResponse.getData();

        } catch (Exception e){
            log.error("[MobCommonService] Exception in calling mob customer for limit validation ={} ", e.getMessage());
            GenericExceptionHandler.handleError(TransferErrorCode.EXTERNAL_SERVICE_ERROR,
                    TransferErrorCode.EXTERNAL_SERVICE_ERROR.getErrorMessage());
        }
        return LimitValidatorResultsDto.builder().build();
    }
}
