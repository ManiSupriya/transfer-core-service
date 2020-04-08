package com.mashreq.transfercoreservice.limits;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitValidator {

    //TODO: Feign call

    /**
     * Method to get the limits and validate against user's consumed limit
     */
    public LimitValidatorResultsDto validate(final UserDTO userDTO, final String beneficiaryType, final BigDecimal paidAmount) throws GenericException {
        return LimitValidatorResultsDto.builder()
                .availableLimitAmount(new BigDecimal("50000"))
                .limitVersionUuid("TEST")
                .build();
    }
}