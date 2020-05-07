package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.banksearch.RoutingCodeType;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

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

    public static List<RoutingCode> extractBankCodes(BeneficiaryDto beneficiaryDto) {
        if (StringUtils.isNotBlank(beneficiaryDto.getRoutingCode())) {
            RoutingCodeType routingCodeType = RoutingCodeType.valueOf(beneficiaryDto.getBeneficiaryCountryISO());
            RoutingCode routingCode = new RoutingCode(routingCodeType.getName() + " CODE", beneficiaryDto.getRoutingCode());
            RoutingCode swiftCode = new RoutingCode("SWIFT", beneficiaryDto.getSwiftCode());
            return Arrays.asList(routingCode, swiftCode);

        }

        if (StringUtils.isNotBlank(beneficiaryDto.getSwiftCode())) {
            return Arrays.asList(new RoutingCode("SWIFT", beneficiaryDto.getSwiftCode()));
        }

        GenericExceptionHandler.handleError(QUICK_REM_ROUTING_CODE_NOT_AVAILABLE, QUICK_REM_ROUTING_CODE_NOT_AVAILABLE.getErrorMessage());
        return null;
    }
}
