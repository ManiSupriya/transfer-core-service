package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferCCMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFundTransferStrategy implements FundTransferStrategy {

    private static final String INDIVIDUAL_ACCOUNT = "I";

    private static final int LOCAL_IBAN_LENGTH = 23;
    private static final String LOCAL_PRODUCT_ID = "DBLC";
    public static final String LOCAL_TRANSACTION_CODE = "15";
    private final IBANValidator ibanValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final CCBelongsToCifValidator ccBelongsToCifValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;
    private final FundTransferMWService fundTransferMWService;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BalanceValidator balanceValidator;
    private final MaintenanceService maintenanceService;
    private final MobCommonService mobCommonService;
    private final DealValidator dealValidator;
    private final CountryRepository countryRepository;
    private final FundTransferCCMWService fundTransferCCMWService;

    @Value("${app.local.currency}")
    private String localCurrency;

    @Value("${app.uae.address}")
    private String address;

    @Autowired
    private QRDealsService qrDealsService;

    @Autowired
    private CardService cardService;


    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO) {
        FundTransferResponse fundTransferResponse;
        if(request.getCardNo() == null && request.getCardNo().trim().length() == 0){
            fundTransferResponse = executeNonCreditCard(request, requestMetaData, userDTO);
        } else {
            fundTransferResponse = executeCC(request, requestMetaData, userDTO);
        }
        return fundTransferResponse;
    }

    private FundTransferResponse executeCC(FundTransferRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO){

       FundTransferResponse fundTransferResponse = null;
        responseHandler(finTxnNoValidator.validate(request, requestMetaData));

        final List<CardDetailsDTO> accountsFromCore = cardService.getCardsFromCore(requestMetaData.getPrimaryCif(), CardType.CC);
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);

        final CardDetailsDTO fromAccountDetails = getSelectedCreditCard(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = execute(request, requestMetaData, validationContext);

        //Balance Validation
        final BigDecimal transferAmountInSrcCurrency = isCurrencySame(beneficiaryDto, fromAccountDetails.getCurrency())
                ? request.getAmount()
                : getAmountInSrcCurrency(request, fromAccountDetails);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, requestMetaData, validationContext));

        //Limit Validation
        final BigDecimal limitUsageAmount = getCCLimitUsageAmount(request.getDealNumber(), fromAccountDetails, transferAmountInSrcCurrency);
        final LimitValidatorResponse validationResult = limitValidator.validateWithProc(userDTO, request.getServiceType(), limitUsageAmount, requestMetaData, null);
        if(validationResult.isValid()){
            fundTransferResponse = processCreditCardTransfer(request, requestMetaData, fromAccountDetails, beneficiaryDto);
        }
        // TODO need to confirm the response if it is failed in the limit validation
        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
    }

    private BeneficiaryDto execute(FundTransferRequestDTO request, RequestMetaData requestMetaData, ValidationContext validationContext) {

        //TODO Remove the empty qrType
        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(), "", INDIVIDUAL_ACCOUNT);

        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, requestMetaData, validationContext));
        if(request.getCardNo() == null) {
            responseHandler(accountBelongsToCifValidator.validate(request, requestMetaData, validationContext));
        } else {
            responseHandler(ccBelongsToCifValidator.validate(request, requestMetaData, validationContext));
        }


        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(requestMetaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), requestMetaData);
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", StringUtils.isBlank(beneficiaryDto.getBeneficiaryCurrency())
                ? localCurrency : beneficiaryDto.getBeneficiaryCurrency());
        responseHandler(beneficiaryValidator.validate(request, requestMetaData, validationContext));


        validationContext.add("iban-length", LOCAL_IBAN_LENGTH);
        responseHandler(ibanValidator.validate(request, requestMetaData, validationContext));

        //Deal Validator
        responseHandler(dealValidator.validate(request, requestMetaData, validationContext));
        return beneficiaryDto;
    }

    private FundTransferResponse executeNonCreditCard(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);

        final AccountDetailsDTO fromAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = execute(request, metadata, validationContext);

        //Balance Validation
        final BigDecimal transferAmountInSrcCurrency = isCurrencySame(beneficiaryDto, fromAccountDetails.getCurrency())
                ? request.getAmount()
                : getAmountInSrcCurrency(request, beneficiaryDto, fromAccountDetails);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validationContext));


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccountDetails, transferAmountInSrcCurrency);
        final LimitValidatorResponse validationResult = limitValidator.validateWithProc(userDTO, request.getServiceType(), limitUsageAmount, metadata, null);



        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountDetails.getCurrency(),
                fromAccountDetails.getBranchCode(), beneficiaryDto);
        log.info("Local Fund transfer initiated.......");

        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private FundTransferResponse processCreditCardTransfer(FundTransferRequestDTO request, RequestMetaData requestMetaData,
                                           CardDetailsDTO cardDetailsDTO, BeneficiaryDto beneficiaryDto){
        // validate the limit on the db
        String cif = requestMetaData.getPrimaryCif();
        FundTransferRequest fundTransferRequest;
        FundTransferResponse fundTransferResponse = null;
        QRDealDetails qrDealDetails = qrDealsService.getQRDealDetails(cif, requestMetaData.getCountry());
        Integer maximumEligibleAmount = qrDealDetails.getTotalLimitAmount();
        Integer utilizedAmount = qrDealDetails.getUtilizedLimitAmount();
        Integer balancedAmount = maximumEligibleAmount - utilizedAmount;
        BigDecimal requestedAmount  = request.getAmount();
        BigDecimal balanceAmount = BigDecimal.valueOf(balancedAmount);
        int result = balanceAmount.compareTo(requestedAmount);
        if(result >= 0){
            // call middleware to process the request
            // TODO need to confirm the branch code
            fundTransferRequest = prepareFundTransferRequestPayload(requestMetaData, request, cardDetailsDTO.getCurrency(),
                    cardDetailsDTO.getSegment(), beneficiaryDto);
            log.info("Local Fund transfer initiated.......");
            fundTransferRequest.setCardNo(request.getCardNo());
            fundTransferRequest.setExpiryDate(cardDetailsDTO.getExpiryDate());
            fundTransferRequest.setSourceISOCurrency(getISOCurrency(fundTransferRequest.getSourceCurrency()));
            fundTransferRequest.setDestinationISOCurrency(getISOCurrency(fundTransferRequest.getDestinationCurrency()));
            fundTransferResponse = fundTransferCCMWService.transfer(fundTransferRequest, requestMetaData);
            // update the utilised amount in the db
            utilizedAmount = utilizedAmount + requestedAmount.intValue();
            qrDealsService.updateQRDeals(cif, utilizedAmount);
        }
        return fundTransferResponse;
    }

    private String getISOCurrency(String currency){
        String code = null;
        Optional<Country> countryOptional = countryRepository.findByLocalCurrencyEqualsIgnoreCase(currency);
        if(countryOptional.isPresent()){
            code = countryOptional.get().getIsoCode2();
        }
        return code;
    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private BigDecimal getCCLimitUsageAmount(final String dealNumber, final CardDetailsDTO cardDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        return "AED".equalsIgnoreCase(cardDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertCCAmountInLocalCurrency(dealNumber, cardDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertCCAmountInLocalCurrency(final String dealNumber, final CardDetailsDTO cardDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(cardDetailsDTO.getCardNo());
        currencyConversionRequestDto.setAccountCurrency(cardDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private boolean isCurrencySame(BeneficiaryDto beneficiaryDto, String sourceAccountCurrency) {
        return sourceAccountCurrency.equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency());
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        BigDecimal amtToBePaidInSrcCurrency;
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency("AED");
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, CardDetailsDTO sourceAccountDetailsDTO) {
        BigDecimal amtToBePaidInSrcCurrency;
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        // TODO need to confirm
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getCardNo());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency("AED");
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  String currency, String branchCode, BeneficiaryDto beneficiaryDto) {
        return FundTransferRequest.builder()
                .productId(LOCAL_PRODUCT_ID)
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(beneficiaryDto.getAccountNumber())
                .purposeCode(request.getPurposeCode())
                .purposeDesc(request.getPurposeDesc())
                .chargeBearer(request.getChargeBearer())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(currency)
                .sourceBranchCode(branchCode)
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .destinationCurrency(localCurrency)
                .awInstName(beneficiaryDto.getBankName())
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .beneficiaryAddressTwo(address)
                .transactionCode(LOCAL_TRANSACTION_CODE)
                .dealNumber(request.getDealNumber())
                .dealRate(request.getDealRate())
                .build();

    }


}
