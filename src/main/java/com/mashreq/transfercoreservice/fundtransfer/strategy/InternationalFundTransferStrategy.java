package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;


/**
 *
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class InternationalFundTransferStrategy implements FundTransferStrategy {

    private static final String INTERNATIONAL_PRODUCT_ID = "DBFC";
    private static final String INTERNATIONAL_VALIDATION_TYPE = "international";
    private static final String INDIVIDUAL_ACCOUNT = "I";
    private static final String ROUTING_CODE_PREFIX = "//";
    public static final String INTERNATIONAL = "International";
    public static final String TRANSACTIONCODE = "15";
    public static final String SPACE_CHAR = " ";
    int maxLength = 35;
    private final FinTxnNoValidator finTxnNoValidator;
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

    @Autowired
    private PostTransactionService postTransactionService;

//    @Value("${app.local.transaction.code:015}")
//    private String transactionCode;

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
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        if (request.getBeneRequiredFields() != null &&
                ((request.getBeneRequiredFields().getMissingFields()!=null && !request.getBeneRequiredFields().getMissingFields().isEmpty()) ||
                (request.getBeneRequiredFields().getIncorrectFields()!=null && !request.getBeneRequiredFields().getIncorrectFields().isEmpty()))){
            log.info("Update missing beneficiary details");
            beneficiaryDto = beneficiaryService.getUpdate(request.getBeneRequiredFields(), Long.valueOf(request.getBeneficiaryId()), metadata, INTERNATIONAL_VALIDATION_TYPE);
        } else {
            beneficiaryDto = beneficiaryService.getById(request.getBeneRequiredFields(), Long.valueOf(request.getBeneficiaryId()), metadata, INTERNATIONAL_VALIDATION_TYPE);
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
        validationContext.add("to-account-currency", beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);

        final BigDecimal transferAmountInSrcCurrency = isCurrencySame(beneficiaryDto,sourceAccountDetailsDTO)
                ? request.getAmount()
                : getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validationContext));


        //Limit Validation
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,transferAmountInSrcCurrency);
        final LimitValidatorResponse validationResult = limitValidator.validateWithProc(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        String txnRefNo = validationResult.getTransactionRefNo();        

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, sourceAccountDetailsDTO, beneficiaryDto, validationResult);
        log.info("International Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata, txnRefNo);

        if(isSuccessOrProcessing(fundTransferResponse)){
        final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),request.getAmount());
        notificationService.sendNotifications(customerNotification,OTHER_ACCOUNT_TRANSACTION,metadata,userDTO);
        fundTransferRequest.setTransferType(INTERNATIONAL);
        fundTransferRequest.setNotificationType(OTHER_ACCOUNT_TRANSACTION);
        fundTransferRequest.setStatus(fundTransferResponse.getResponseDto().getMwResponseStatus().getName());
        postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest);
        }

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).transactionRefNo(txnRefNo).build();
    }

    private boolean isCurrencySame(BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        return sourceAccountDetailsDTO.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency());
    }
    
    private CustomerNotification populateCustomerNotification(String transactionRefNo, String currency, BigDecimal amount) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(currency);
        customerNotification.setTxnRef(transactionRefNo);
        return customerNotification;
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        //currencyConversionRequestDto.setDealNumber(dealNumber);
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
        currencyRequest.setDealNumber(request.getDealNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency(request.getTxnCurrency());
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto, LimitValidatorResponse validationResult) {

        String beneFullName =StringUtils.isNotBlank(beneficiaryDto.getFullName())&& beneficiaryDto.getFullName().length() > maxLength?beneficiaryDto.getFullName().substring(maxLength)+" ":"";
        String add1 =StringUtils.isNotBlank(beneficiaryDto.getAddressLine1())?beneficiaryDto.getAddressLine1()+" ":"";
        String add2 =StringUtils.isNotBlank(beneficiaryDto.getAddressLine2())?beneficiaryDto.getAddressLine2()+" ":"";
        String add3 =StringUtils.isNotBlank(beneficiaryDto.getAddressLine3())?beneficiaryDto.getAddressLine3():"";


    	String finalAdd1= StringUtils.left(beneFullName+add1,maxLength);
        String finalAdd2 = null;
    	if(StringUtils.isNotBlank(finalAdd1) && finalAdd1.length()>maxLength)
            finalAdd2 = StringUtils.left(finalAdd1.substring(maxLength)+add2+add3,maxLength);
    	else
            finalAdd2 = StringUtils.left(add2+add3,maxLength);

    	/*if(StringUtils.isNotBlank(beneficiaryDto.getAddressLine2()) && StringUtils.isNotBlank(beneficiaryDto.getAddressLine3())){
    		address3 = StringUtils.left(beneficiaryDto.getAddressLine2().concat(SPACE_CHAR+beneficiaryDto.getAddressLine3()), maxLength);
    	} else if(StringUtils.isNotBlank(beneficiaryDto.getAddressLine2()) && StringUtils.isBlank(beneficiaryDto.getAddressLine3())){
    		address3 = StringUtils.left(beneficiaryDto.getAddressLine2(), maxLength);
    	} else if(StringUtils.isBlank(beneficiaryDto.getAddressLine2()) && StringUtils.isNotBlank(beneficiaryDto.getAddressLine3())){
    		address3 = StringUtils.left(beneficiaryDto.getAddressLine3(), maxLength);
    	}*/
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
                .beneficiaryFullName(StringUtils.isNotBlank(beneficiaryDto.getFullName()) && beneficiaryDto.getFullName().length() > maxLength? StringUtils.left(beneficiaryDto.getFullName(), maxLength): beneficiaryDto.getFullName())
                .beneficiaryAddressOne(finalAdd1)
                .beneficiaryAddressThree(finalAdd2)
                .destinationCurrency(request.getTxnCurrency())
                .transactionCode(TRANSACTIONCODE)
                .dealNumber(request.getDealNumber())
                .txnCurrency(request.getTxnCurrency())
                .dealRate(request.getDealRate())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .finalBene(request.getFinalBene())
                .additionaField(request.getAdditionalField())
                .build();

        return enrichFundTransferRequestByCountryCode(fundTransferRequest, beneficiaryDto);
    }

    private FundTransferRequest enrichFundTransferRequestByCountryCode(FundTransferRequest request, BeneficiaryDto beneficiaryDto) {
        List<CountryMasterDto> countryList = maintenanceService.getAllCountries("MOB", "AE", Boolean.TRUE);
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
                        .beneficiaryAddressTwo(StringUtils.isNotBlank(countryMasterDto.getName())?countryMasterDto.getName().toUpperCase():countryMasterDto.getName())
                        .build();
            }
        }
        return request.toBuilder()
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .awInstName(beneficiaryDto.getBankName())
                .beneficiaryAddressTwo(countryDto.isPresent()&&StringUtils.isNotBlank(countryDto.get().getName())?countryDto.get().getName().toUpperCase():null)
                .build();
    }
    
    private boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }


}
