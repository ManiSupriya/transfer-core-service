package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.service.AccountService;


import com.mashreq.transfercoreservice.fundtransfer.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Long.valueOf;

/**
 *
 */
@AllArgsConstructor
@Slf4j
@Service
public class InternationalFundTransferStrategy implements FundTransferStrategy {

    private static final String INTERNATIONAL = "INTERNATIONAL";
    private static final String INTERNATIONAL_PRODUCT_ID = "DBFC";
    private static final String ROUTING_CODE_PREFIX = "//";
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountService accountService;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final MaintenanceClient maintenanceClient;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final IBANValidator ibanValidator;
    private final BalanceValidator balanceValidator;
    private final RoutingCodeValidator routingCodeValidator;
    private final SwiftCodeValidator swiftCodeValidator;
    private final FundTransferMWService fundTransferMWService;

    private final BeneficiaryClient beneficiaryClient;
    private final LimitValidator limitValidator;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final Set<PurposeOfTransferDto> allPurposeCodes = maintenanceClient.getAllPurposeCodes(INTERNATIONAL).getData();
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));
        log.info("Purpose code and description validation successful");

        final BeneficiaryDto beneficiaryDto = beneficiaryClient.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()))
                .getData();
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));
        log.info("Beneficiary validation successful");

        final AccountDetailsDTO accountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        request.setCurrency(accountDetailsDTO.getCurrency());

        responseHandler(swiftCodeValidator.validate(request, metadata, validationContext));
        //validateIbanRoutingCodes(request, metadata, validationContext, beneficiaryDto);

        validationContext.add("from-account", accountDetailsDTO);
        responseHandler(balanceValidator.validate(request, metadata, validationContext));
        log.info("Balance validation successful");

        log.info("Limit Validation start.");
        BigDecimal limitUsageAmount = request.getAmount();
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit validation successful");

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, accountDetailsDTO, beneficiaryDto);
        log.info("International Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
    }

    private void validateIbanRoutingCodes(FundTransferRequestDTO request, FundTransferMetadata metadata,
                                          ValidationContext validationContext, BeneficiaryDto beneficiaryDto) {

        final String countryISO = beneficiaryDto.getBankCountryISO();
        final List<CountryDto> allCountries = maintenanceClient.getAllCountries().getData();
        final Optional<CountryDto> beneficiaryCountry = getBeneficiaryCountry(countryISO, allCountries);
        /*if(beneficiaryCountry.isPresent()) {
            if(beneficiaryCountry.get().getIban().getRequired()) {
                validationContext.add("iban-length",beneficiaryCountry.get().getIban().getLength());
                responseHandler(ibanValidator.validate(request, metadata, validationContext));
            }

            if(beneficiaryCountry.get().getRoutingCode().getRequired()) {
                validationContext.add("routing-code-length",beneficiaryCountry.get().getRoutingCode().getLength());
                responseHandler(routingCodeValidator.validate(request, metadata, validationContext));

            }
        }*/
    }


    private Optional<CountryDto> getBeneficiaryCountry(String countryISO, List<CountryDto> allCountries) {
        return allCountries.stream().filter(country -> country.getCode().equals(countryISO)).findAny();
    }

    private FundTransferRequest prepareFundTransferRequestPayload(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto) {
        final FundTransferRequest req = FundTransferRequest.builder()
                .productId(INTERNATIONAL_PRODUCT_ID)
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .purposeCode(request.getPurposeCode())
                .purposeDesc(request.getPurposeDesc())
                .chargeBearer(request.getChargeBearer())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(accountDetails.getCurrency())
                .sourceBranchCode(accountDetails.getBranchCode())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .destinationCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .beneficiaryAddressOne(beneficiaryDto.getAddressLine1())
                .beneficiaryAddressTwo(beneficiaryDto.getAddressLine2())
                .beneficiaryAddressThree(beneficiaryDto.getAddressLine3())
                .isCreditLegAmount(false)
                .build();

        if (isRoutingCodeCountry(beneficiaryDto.getRoutingCode())) {
            return req.toBuilder()
                    .awInstBICCode(ROUTING_CODE_PREFIX + beneficiaryDto.getRoutingCode())
                    .awInstName(beneficiaryDto.getSwiftCode())
                    .build();
        } else {
            return req.toBuilder()
                    .awInstBICCode(beneficiaryDto.getSwiftCode())
                    .awInstName(beneficiaryDto.getBankName())
                    .build();
        }
    }

    private boolean isRoutingCodeCountry(String routingCode) {
        return StringUtils.isNotBlank(routingCode);
    }

    private AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }

}
