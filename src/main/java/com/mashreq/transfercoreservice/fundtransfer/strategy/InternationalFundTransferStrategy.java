package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonUtils;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.AddressLineSeparatorUtil;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.INFT_TRANSACTION;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;
import static java.util.Optional.ofNullable;


/**
 *
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Service
public class InternationalFundTransferStrategy implements FundTransferStrategy {

    private static final String INTERNATIONAL_PRODUCT_ID = "DBFC";
    private static final String INTERNATIONAL_VALIDATION_TYPE = "international";
    private static final String INDIVIDUAL_ACCOUNT = "I";
    private static final String ROUTING_CODE_PREFIX = "//";
    public static final String INTERNATIONAL = "International";
    public static final String TRANSACTIONCODE = "15";
    public static final String SPACE_CHAR = " ";

    public static final String TRANSFER_AMOUNT_FOR_MIN_VALIDATION = "transfer-amount-for-min-validation";
    int maxLength = 35;
    private final AccountService accountService;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final FundTransferMWService fundTransferMWService;
    private final MaintenanceService maintenanceService;
    private final MobCommonService mobCommonService;
    private final DealValidator dealValidator;
    private final NotificationService notificationService;

    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;

    private final HashMap<String, String> routingSuffixMap = new HashMap<>();
    private final CCTransactionEligibilityValidator ccTrxValidator;
    
    private final CurrencyValidator currencyValidator;

    private final MinTransactionAmountValidator minTransactionAmountValidator;

    @Autowired
    private PostTransactionService postTransactionService;

    @Value("${app.local.currency}")
    private String localCurrency;
    @Value("${app.local.country.iso}")
    private String localCountryIso;

    //Todo: Replace with native currency fetched from API call
    @PostConstruct
    private void initRoutingPrefixMap() {
        routingSuffixMap.put("IN", "/");
        routingSuffixMap.put("AU", "/BSB");
        routingSuffixMap.put("CA", "/");
        routingSuffixMap.put("NZ", "/BSB");
        routingSuffixMap.put("GB", "/SC");
        routingSuffixMap.put("US", "/FW");
    }

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {
    	responseHandler(ccTrxValidator.validate(request, metadata));
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(),
                "", INDIVIDUAL_ACCOUNT);
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        if (request.getBeneRequiredFields() != null &&
                ((request.getBeneRequiredFields().getMissingFields()!=null && !request.getBeneRequiredFields().getMissingFields().isEmpty()) ||
                (request.getBeneRequiredFields().getIncorrectFields()!=null && !request.getBeneRequiredFields().getIncorrectFields().isEmpty()))){
            log.info("Update missing beneficiary details");
            beneficiaryDto = beneficiaryService.getUpdate(request.getBeneRequiredFields(), Long.valueOf(request.getBeneficiaryId()), request.getJourneyVersion(), metadata, INTERNATIONAL_VALIDATION_TYPE);
        } else {
            beneficiaryDto = beneficiaryService.getById(Long.valueOf(request.getBeneficiaryId()), request.getJourneyVersion(), metadata, INTERNATIONAL_VALIDATION_TYPE);
        }
        
        
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));

        //Deal Validator
        log.info("Deal Validation Started");
        if (StringUtils.isNotBlank(request.getDealNumber()) && !request.getDealNumber().isEmpty()) {
            responseHandler(dealValidator.validate(request, metadata, validationContext));
   		 }
        
        //Balance Validation
        final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", sourceAccountDetailsDTO);
        
        responseHandler(currencyValidator.validate(request, metadata, validationContext));

        final CurrencyConversionDto currencyConversionDto = validateAccountBalance(request, metadata, validationContext, beneficiaryDto, sourceAccountDetailsDTO);
        final BigDecimal transferAmountInSrcCurrency = currencyConversionDto.getAccountCurrencyAmount();

        //Limit Validation
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final BigDecimal limitUsageAmount = getLimitUsageAmount(sourceAccountDetailsDTO,transferAmountInSrcCurrency);

        validationContext.add(TRANSFER_AMOUNT_FOR_MIN_VALIDATION, limitUsageAmount );
        responseHandler(minTransactionAmountValidator.validate(request, metadata, validationContext));

        final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        String txnRefNo = validationResult.getTransactionRefNo();        

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, sourceAccountDetailsDTO, beneficiaryDto, validationResult, currencyConversionDto);
        log.info("International Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = processTransaction(metadata, txnRefNo, fundTransferRequest,request);

        handleSuccessfullTransaction(request, metadata, userDTO, validationResult, fundTransferRequest,
				fundTransferResponse , beneficiaryDto);

        return prepareResponse(transferAmountInSrcCurrency, limitUsageAmount, validationResult, txnRefNo, fundTransferResponse);
    }

	protected FundTransferResponse prepareResponse(final BigDecimal transferAmountInSrcCurrency,
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult, String txnRefNo,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .debitAmount(transferAmountInSrcCurrency)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).transactionRefNo(txnRefNo).build();
	}

	protected void handleSuccessfullTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
                                                UserDTO userDTO, final LimitValidatorResponse validationResult,
                                                final FundTransferRequest fundTransferRequest, final FundTransferResponse fundTransferResponse, BeneficiaryDto beneficiaryDto) {
		if(isSuccessOrProcessing(fundTransferResponse)){
        final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),
                request.getTxnCurrency(),request.getAmount(),fundTransferRequest.getBeneficiaryFullName(), fundTransferRequest.getToAccount());
        notificationService.sendNotifications(customerNotification, INFT_TRANSACTION, metadata,userDTO);
        fundTransferRequest.setTransferType(INTERNATIONAL);
        fundTransferRequest.setNotificationType(OTHER_ACCOUNT_TRANSACTION);
        fundTransferRequest.setStatus(fundTransferResponse.getResponseDto().getMwResponseStatus().getName());
        postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest, request, ofNullable(beneficiaryDto));
        }
	}

	protected FundTransferResponse processTransaction(RequestMetaData metadata, String txnRefNo,
			final FundTransferRequest fundTransferRequest, FundTransferRequestDTO request) {
		final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata, txnRefNo);
		return fundTransferResponse;
	}

	protected CurrencyConversionDto validateAccountBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validationContext, BeneficiaryDto beneficiaryDto,
			final AccountDetailsDTO sourceAccountDetailsDTO) {
		final CurrencyConversionDto currencyConversionDto = getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);
        validationContext.add("transfer-amount-in-source-currency", currencyConversionDto.getAccountCurrencyAmount());
        responseHandler(balanceValidator.validate(request, metadata, validationContext));
		return currencyConversionDto;
	}
    
    protected CustomerNotification populateCustomerNotification(String transactionRefNo, String currency, BigDecimal amount, String beneficiaryName, String creditAccount) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(currency);
        customerNotification.setTxnRef(transactionRefNo);
        customerNotification.setCreditAccount(creditAccount);
        customerNotification.setBeneficiaryName(beneficiaryName);
        return customerNotification;
    }

    private BigDecimal convertAmountInLocalCurrency(final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setTransactionCurrency(localCurrency);
        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private BigDecimal getLimitUsageAmount(final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        return localCurrency.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    protected CurrencyConversionDto getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto,
                                              AccountDetailsDTO sourceAccountDetailsDTO) {
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setDealNumber(request.getDealNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency(request.getTxnCurrency());
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto currencyConversionDto =  maintenanceService.convertBetweenCurrencies(currencyRequest);
        currencyConversionDto.setExchangeRateDisplayTxt(CommonUtils.generateDisplayString(currencyConversionDto, currencyRequest));
        return currencyConversionDto;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto,
                                                                  LimitValidatorResponse validationResult, CurrencyConversionDto currencyConversionDto) {
    	/** applying logic to separate address and full name to four lines with specified max length*/
    	String[] addresslines = AddressLineSeparatorUtil.separateAddressLineForSwift(maxLength,beneficiaryDto.getFullName(),beneficiaryDto.getAddressLine1(),beneficiaryDto.getAddressLine2(),beneficiaryDto.getAddressLine3());
    	/** updating beneficiary final name from beneficiary dto if it is present 
    	 * added this because in new money transfer journey there is no option in UI for user to input this values during money transfer
    	 * this is handled in beneficiary module only */
    	if(StringUtils.isBlank(request.getFinalBene()) && StringUtils.isNotBlank(beneficiaryDto.getFinalName())) {
    		request.setFinalBene(beneficiaryDto.getFinalName());
    	}
    	
    	final FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .productId(INTERNATIONAL_PRODUCT_ID)
                .srcCcyAmt(currencyConversionDto.getAccountCurrencyAmount())
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
                .beneficiaryFullName(StringUtils.isNotBlank(beneficiaryDto.getFullName()) && beneficiaryDto.getFullName().length() > maxLength? StringUtils.left(beneficiaryDto.getFullName(), maxLength): beneficiaryDto.getFullName())
                .beneficiaryAddressOne(addresslines[0])
                .beneficiaryAddressTwo(addresslines[1])
                .beneficiaryAddressThree(addresslines[2])
                .destinationCurrency(request.getTxnCurrency())
                .transactionCode(TRANSACTIONCODE)
                .dealNumber(request.getDealNumber())
                .txnCurrency(request.getTxnCurrency())
                .dealRate(request.getDealRate())
                .intermediaryBankSwiftCode(request.getIntermediaryBankSwiftCode())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .finalBene(request.getFinalBene())
                .paymentNote(StringUtils.isEmpty(request.getAdditionalField())?request.getPaymentNote():request.getAdditionalField())
                .serviceType(request.getServiceType())
                .accountClass(accountDetails.getAccountType())
                .exchangeRateDisplayTxt(currencyConversionDto.getExchangeRateDisplayTxt())
                .build();

        return enrichFundTransferRequestByCountryCode(fundTransferRequest, beneficiaryDto);
    }

    private FundTransferRequest enrichFundTransferRequestByCountryCode(FundTransferRequest request, BeneficiaryDto beneficiaryDto) {
        List<CountryMasterDto> countryList = maintenanceService.getAllCountries("MOB", localCountryIso, Boolean.TRUE);
        final Optional<CountryMasterDto> countryDto = countryList.stream()
                .filter(country -> country.getCode().equals(beneficiaryDto.getBankCountryISO()))
                .findAny();


        if (countryDto.isPresent()) {
            final CountryMasterDto countryMasterDto = countryDto.get();
            if (StringUtils.isNotBlank(routingSuffixMap.get(beneficiaryDto.getBankCountryISO())) && request.getTxnCurrency()
                    .equalsIgnoreCase(countryMasterDto.getNativeCurrency()) && StringUtils.isNotBlank(beneficiaryDto.getRoutingCode())) {

                 log.info("Routing Prefix for fund transfer: "+ROUTING_CODE_PREFIX + routingSuffixMap.get(beneficiaryDto.getBankCountryISO()) + beneficiaryDto.getRoutingCode());
                return request.toBuilder()
                        .awInstBICCode(routingSuffixMap.get(beneficiaryDto.getBankCountryISO()) + beneficiaryDto.getRoutingCode())
                        .awInstName(beneficiaryDto.getSwiftCode())
                        .beneficiaryBankCountry(StringUtils.isNotBlank(countryMasterDto.getName())?countryMasterDto.getName().toUpperCase():countryMasterDto.getName())
                        .build();
            }
        }
        return request.toBuilder()
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .awInstName(beneficiaryDto.getBankName())
                .beneficiaryBankCountry(countryDto.isPresent()&&StringUtils.isNotBlank(countryDto.get().getName())?countryDto.get().getName().toUpperCase():null)
                .build();
    }
    
    private boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }
}
