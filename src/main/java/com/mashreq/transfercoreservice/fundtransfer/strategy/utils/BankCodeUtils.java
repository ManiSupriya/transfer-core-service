package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.banksearch.RoutingCodeType;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode;
import org.apache.commons.lang3.StringUtils;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.QUICK_REM_ROUTING_CODE_NOT_AVAILABLE;

/**
 * @author shahbazkh
 * @date 5/7/20
 */
public class BankCodeUtils {

    public static RoutingCode extractBankCode(BeneficiaryDto beneficiaryDto) {
        if (StringUtils.isNotBlank(beneficiaryDto.getRoutingCode())) {
            RoutingCodeType routingCodeType = RoutingCodeType.valueOf(beneficiaryDto.getBankCountry());
            return new RoutingCode(routingCodeType.getName(), beneficiaryDto.getRoutingCode());
        }

        if (StringUtils.isNotBlank(beneficiaryDto.getSwiftCode())) {
            return new RoutingCode("SWIFT", beneficiaryDto.getSwiftCode());
        }

        GenericExceptionHandler.handleError(QUICK_REM_ROUTING_CODE_NOT_AVAILABLE, QUICK_REM_ROUTING_CODE_NOT_AVAILABLE.getErrorMessage());
        return null;
    }
}
