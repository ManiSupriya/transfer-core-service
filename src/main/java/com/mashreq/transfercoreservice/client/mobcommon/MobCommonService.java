package com.mashreq.transfercoreservice.client.mobcommon;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@TrackExec
public class MobCommonService {

    private final MobCommonClient mobCommonClient;

    public LimitValidatorResultsDto validateAvailableLimit(String cifId, String beneficiaryTypeCode, BigDecimal amount){

        log.info("[MobCommonService] Calling MobCommonService for limit validation for cif={} beneficiaryTypeCode = {} and amount ={}",
                cifId, beneficiaryTypeCode, amount);

        Response<LimitValidatorResultsDto> limitValidatorResultsDtoResponse =
                mobCommonClient.validateAvailableLimit(cifId, beneficiaryTypeCode, amount);

        //TODO : handler error

        log.info("[MobCommonService] MobCommonService response success");
        return limitValidatorResultsDtoResponse.getData();
    }
}
