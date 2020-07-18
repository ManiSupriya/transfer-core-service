package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Long.valueOf;

/**
 *
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class InternationalFundTransferStrategy implements FundTransferStrategy {

    private static final String INTERNATIONAL_PRODUCT_ID = "DBFC";
    private static final String INDIVIDUAL_ACCOUNT = "I";
    private static final String ROUTING_CODE_PREFIX = "//";
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountService accountService;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final FundTransferMWService fundTransferMWService;
    private final MaintenanceService maintenanceService;
    private final MobCommonService mobCommonService;

    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;

    private final HashMap<String, String> countryToCurrencyMap = new HashMap<>();

//    @Value("${app.local.transaction.code:015}")
//    private String transactionCode;

    //Todo: Replace with native currency fetched from API call
    @PostConstruct
    private void initCountryToNativeCurrencyMap() {
        countryToCurrencyMap.put("IN", "INR");
        countryToCurrencyMap.put("AU", "AUD");
        countryToCurrencyMap.put("CA", "CAD");
        countryToCurrencyMap.put("NZ", "NZD");
        countryToCurrencyMap.put("UK", "GBP");
        countryToCurrencyMap.put("US", "USD");
    }

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(),
                "", INDIVIDUAL_ACCOUNT);
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));


        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()));
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));


        //Balance Validation
        final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("to-account-currency", beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);

        final BigDecimal transferAmountInSrcCurrency = isCurrencySame(beneficiaryDto,sourceAccountDetailsDTO)
                ? request.getAmount()
                : getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validationContext));


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,transferAmountInSrcCurrency);
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata);


        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, sourceAccountDetailsDTO, beneficiaryDto);
        log.info("International Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
    }

    private boolean isCurrencySame(BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        return sourceAccountDetailsDTO.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency());
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
        return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto,
                                              AccountDetailsDTO sourceAccountDetailsDTO) {
        BigDecimal amtToBePaidInSrcCurrency;
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency(beneficiaryDto.getBeneficiaryCurrency());
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto) {
        final FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .productId(INTERNATIONAL_PRODUCT_ID)
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(beneficiaryDto.getAccountNumber())
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
                .transactionCode("15")
                .build();

        return enrichFundTransferRequestByCountryCode(fundTransferRequest, beneficiaryDto);
    }

    private FundTransferRequest enrichFundTransferRequestByCountryCode(FundTransferRequest request, BeneficiaryDto beneficiaryDto) {
        List<CountryMasterDto> countryList = maintenanceService.getAllCountries("MOB", "AE", Boolean.TRUE);
        final Optional<CountryMasterDto> countryDto = countryList.stream()
                .filter(country -> country.getCode().equals(beneficiaryDto.getBeneficiaryCountryISO()))
                .findAny();
        if (countryDto.isPresent()) {
            final CountryMasterDto countryMasterDto = countryDto.get();
            if (StringUtils.isNotBlank(countryMasterDto.getRoutingCode()) && request.getDestinationCurrency()
                    .equals(countryToCurrencyMap.get(beneficiaryDto.getBeneficiaryCountryISO()))) {

                return request.toBuilder()
                        .awInstBICCode(ROUTING_CODE_PREFIX + beneficiaryDto.getRoutingCode())
                        .awInstName(beneficiaryDto.getSwiftCode())
                        .build();
            }
        }
        return request.toBuilder()
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .awInstName(beneficiaryDto.getBankName())
                .build();
    }


}
