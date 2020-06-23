package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.QUICK_REM_COUNTRY_CODE_NOT_FOUND;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.QUICK_REM_SWIFT_CODE_NOT_FOUND;

/**
 * @author shahbazkh
 * @date 5/20/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BankCodeExtractor {

    public static final String SUBSTR_BEFORE_ASSIGNING_TO_ROUTING_CODE = "substr-index-before-assigning-to-routing-code";
    private final InstaRemCountryRules instaRemCountryRules;

    public List<RoutingCode> getRoutingCodes(BeneficiaryDto beneficiaryDto) {

        if (StringUtils.isBlank(beneficiaryDto.getSwiftCode()))
            GenericExceptionHandler.handleError(QUICK_REM_SWIFT_CODE_NOT_FOUND, QUICK_REM_SWIFT_CODE_NOT_FOUND.getErrorMessage());

        if (StringUtils.isBlank(beneficiaryDto.getBeneficiaryCountryISO()))
            GenericExceptionHandler.handleError(QUICK_REM_COUNTRY_CODE_NOT_FOUND, QUICK_REM_COUNTRY_CODE_NOT_FOUND.getErrorMessage());

        final Map<String, Map<String, String>> countryBankCodeLookUp = instaRemCountryRules.getBankCodeType();
        final String countryCode = beneficiaryDto.getBeneficiaryCountryISO();


        if (countryBankCodeLookUp.containsKey(beneficiaryDto.getBeneficiaryCountryISO())) {
            RoutingCode swiftCode = new RoutingCode("SWIFT", beneficiaryDto.getSwiftCode());

            final String routingCodeType = countryBankCodeLookUp.get(countryCode).get("code");
            final String routingCodeValue = evaluateRuleAndGetCode(countryBankCodeLookUp.get(countryCode), beneficiaryDto.getRoutingCode());
            RoutingCode routingCode = new RoutingCode(routingCodeType, routingCodeValue);

            return Arrays.asList(routingCode, swiftCode);
        }

        return Arrays.asList(new RoutingCode("SWIFT", beneficiaryDto.getSwiftCode()));
    }

    private String evaluateRuleAndGetCode(final Map<String, String> map, final String routingCodeValue) {
        if (map.containsKey(SUBSTR_BEFORE_ASSIGNING_TO_ROUTING_CODE)) {
            int subStringIndex = Integer.parseInt(map.get(SUBSTR_BEFORE_ASSIGNING_TO_ROUTING_CODE));
            return routingCodeValue.substring(subStringIndex);
        }

        return routingCodeValue;
    }
}
