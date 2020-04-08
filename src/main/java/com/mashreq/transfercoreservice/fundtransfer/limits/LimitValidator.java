package com.mashreq.transfercoreservice.fundtransfer.limits;

import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitValidator {

    private final MobCommonService mobCommonService;

    /**
     * Method to get the limits and validate against user's consumed limit
     */
    public LimitValidatorResultsDto validate(final UserDTO userDTO, final String beneficiaryType, final BigDecimal paidAmount) {

        //TODO: remove below commented code once testing done
//        return LimitValidatorResultsDto.builder()
//                .availableLimitAmount(new BigDecimal("50000"))
//                .limitVersionUuid("TEST")
//                .build();

        log.info("[LimitValidator] limit validator called cif ={} and beneficiaryType={} and paidAmount={}",
                userDTO.getCifId(), beneficiaryType, paidAmount);
        return mobCommonService.validateAvailableLimit(userDTO.getCifId(), beneficiaryType, paidAmount);

    }
}