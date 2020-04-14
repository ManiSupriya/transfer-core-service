package com.mashreq.transfercoreservice.fundtransfer.limits;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitCheckType;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
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
        log.info("[LimitValidator] limit validator called cif ={} and beneficiaryType={} and paidAmount={}",
                userDTO.getCifId(), beneficiaryType, paidAmount);
        LimitValidatorResultsDto limitValidatorResultsDto =
                mobCommonService.validateAvailableLimit(userDTO.getCifId(), beneficiaryType, paidAmount);

        if (!limitValidatorResultsDto.isValid()) {
            if (LimitCheckType.DAILY_AMOUNT.equals(limitValidatorResultsDto.getLimitCheckType())) {
                GenericExceptionHandler.handleError(TransferErrorCode.DAY_AMOUNT_LIMIT_REACHED,
                        TransferErrorCode.DAY_AMOUNT_LIMIT_REACHED.getErrorMessage());
            } else {
                GenericExceptionHandler.handleError(TransferErrorCode.MONTH_AMOUNT_LIMIT_REACHED,
                        TransferErrorCode.MONTH_AMOUNT_LIMIT_REACHED.getErrorMessage());
            }
        }
        log.info("Limit validation successful");
        return limitValidatorResultsDto;
    }
}