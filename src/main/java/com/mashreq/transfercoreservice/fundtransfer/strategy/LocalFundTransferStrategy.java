package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferCCMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;
import static java.lang.Long.valueOf;

/**
 *
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFundTransferStrategy implements FundTransferStrategy {

    private static final String INDIVIDUAL_ACCOUNT = "I";
    private static final String SOURCE_OF_FUND_CC = "Credit Card";


    private static final int LOCAL_IBAN_LENGTH = 23;
    private static final String LOCAL_PRODUCT_ID = "DBLC";
    public static final String LOCAL_TRANSACTION_CODE = "15";
    public static final String SPACE_CHAR = " ";
    int maxLength = 35;
    public static final String AED = "AED";
    public static final String NON_AED = "non-AED";
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
    private final CCBalanceValidator ccBalanceValidator;
    private final MaintenanceService maintenanceService;
    private final MobCommonService mobCommonService;
    private final DealValidator dealValidator;
    private final CountryRepository countryRepository;
    private final FundTransferCCMWService fundTransferCCMWService;
    private final AsyncUserEventPublisher auditEventPublisher;
    private final EncryptionService encryptionService = new EncryptionService();
    private final NotificationService notificationService;

    @Value("${app.local.currency}")
    private String localCurrency;

    @Value("${app.uae.address}")
    private String address;

    @Autowired
    private QRDealsService qrDealsService;

    @Autowired
    private CardService cardService;

    @Autowired
    private PostTransactionService postTransactionService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO) {
        FundTransferResponse fundTransferResponse;
        if(StringUtils.isBlank(request.getCardNo())){
            fundTransferResponse = executeNonCreditCard(request, requestMetaData, userDTO);
        } else {
            fundTransferResponse = executeCC(request, requestMetaData, userDTO);
        }
        return fundTransferResponse;
    }

    /**
     * Method is used to initiate the fund transfer request except CC
     * @param request
     * @param metadata
     * @param userDTO
     * @return
     */
    private FundTransferResponse executeNonCreditCard(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);


        //TODO Remove the empty qrType
        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(), "", INDIVIDUAL_ACCOUNT);

        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final AccountDetailsDTO fromAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()), metadata);
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", localCurrency);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));


        validationContext.add("iban-length", LOCAL_IBAN_LENGTH);
        responseHandler(ibanValidator.validate(request, metadata, validationContext));

        //Deal Validator
        log.info("Deal Validation Started");
        if (StringUtils.isNotBlank(request.getDealNumber()) && !request.getDealNumber().isEmpty()) {
        	String trxCurrency = StringUtils.isBlank(request.getTxnCurrency()) ? localCurrency
					: request.getTxnCurrency();
			if (StringUtils.equalsIgnoreCase(trxCurrency, request.getCurrency())) {
				auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
						CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.toString(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage());
				GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY,
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage());
			}
			request.setTxnCurrency(trxCurrency);
            responseHandler(dealValidator.validate(request, metadata, validationContext));
   		 }

        //Balance Validation
        final BigDecimal transferAmountInSrcCurrency = getAmountInSrcCurrency(request, beneficiaryDto, fromAccountDetails);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validationContext));


        //Limit Validation
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccountDetails, transferAmountInSrcCurrency);
        final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        String txnRefNo = validationResult.getTransactionRefNo();
        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountDetails.getCurrency(), fromAccountDetails.getBranchCode(), beneficiaryDto, validationResult);
        log.info("Local Fund transfer initiated.......");

        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata, txnRefNo);

        if(isSuccessOrProcessing(fundTransferResponse)){
            final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),request.getAmount());
            notificationService.sendNotifications(customerNotification,OTHER_ACCOUNT_TRANSACTION,metadata,userDTO);
            fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getTxnCurrency()));
            fundTransferRequest.setNotificationType(OTHER_ACCOUNT_TRANSACTION);
            fundTransferRequest.setStatus(fundTransferResponse.getResponseDto().getMwResponseStatus().getName());
            postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest);
        }

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).transactionRefNo(txnRefNo).build();

    }

    /**
     * Method is used to initiate the Fund transfer for the Credit card
     * @param request
     * @param requestMetaData
     * @param userDTO
     * @return
     */
    private FundTransferResponse executeCC(FundTransferRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO){

        FundTransferResponse fundTransferResponse;

        responseHandler(finTxnNoValidator.validate(request, requestMetaData));

        final List<CardDetailsDTO> accountsFromCore = cardService.getCardsFromCore(requestMetaData.getPrimaryCif(), CardType.CC);
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);

        final CardDetailsDTO selectedCreditCard = getSelectedCreditCard(accountsFromCore, request.getCardNo());
        validationContext.add("from-account", selectedCreditCard);

        final BeneficiaryDto beneficiaryDto = validateBeneficiary(request, requestMetaData, validationContext);

        //Balance Validation
        // TODO comment tbe below transfer amount in src currency for CC
      /*  final BigDecimal transferAmountInSrcCurrency = isCurrencySame(beneficiaryDto, request.getCurrency())
                ? request.getAmount()
                : getAmountInSrcCurrency(request, selectedCreditCard);*/

        final BigDecimal transferAmountInSrcCurrency = request.getAmount();
        
        String trxCurrency = StringUtils.isBlank(request.getTxnCurrency()) ? localCurrency
				: request.getTxnCurrency();

        request.setTxnCurrency(trxCurrency);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(ccBalanceValidator.validate(request, requestMetaData, validationContext));

        //Limit Validation
        // TODO comment tbe below limit usage amount check for CC
       // final BigDecimal limitUsageAmount = getCCLimitUsageAmount(request.getDealNumber(), selectedCreditCard, transferAmountInSrcCurrency);

        final BigDecimal limitUsageAmount = transferAmountInSrcCurrency;
         final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, requestMetaData, null);
         String txnRefNo = validationResult.getTransactionRefNo();
         fundTransferResponse = processCreditCardTransfer(request, requestMetaData, selectedCreditCard, beneficiaryDto, validationResult);
         if(isSuccessOrProcessing(fundTransferResponse)){
             final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),request.getAmount());
             notificationService.sendNotifications(customerNotification,OTHER_ACCOUNT_TRANSACTION,requestMetaData,userDTO);
             }
         
        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).transactionRefNo(txnRefNo).build();
    }

    /**
     * Get the beneficiary details and validate the details.
     * @param request
     * @param requestMetaData
     * @param validationContext
     * @return
     */
    private BeneficiaryDto validateBeneficiary(FundTransferRequestDTO request, RequestMetaData requestMetaData, ValidationContext validationContext) {

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(), "", INDIVIDUAL_ACCOUNT);

        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, requestMetaData, validationContext));
        responseHandler(ccBelongsToCifValidator.validate(request, requestMetaData, validationContext));

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(requestMetaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), requestMetaData);
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", localCurrency);
        responseHandler(beneficiaryValidator.validate(request, requestMetaData, validationContext));

        validationContext.add("iban-length", LOCAL_IBAN_LENGTH);
        responseHandler(ibanValidator.validate(request, requestMetaData, validationContext));

        //Deal Validator
        responseHandler(dealValidator.validate(request, requestMetaData, validationContext));
        return beneficiaryDto;
    }

    /**
     * Used to validate the QR deals for the given cif, validate the available balance, call MW and
     * if it is success, update the balance amount
     * @param requestDTO
     * @param requestMetaData
     * @param cardDetailsDTO
     * @param beneficiaryDto
     * @return
     */
    private FundTransferResponse processCreditCardTransfer(FundTransferRequestDTO requestDTO, RequestMetaData requestMetaData,
                                           CardDetailsDTO cardDetailsDTO, BeneficiaryDto beneficiaryDto, LimitValidatorResponse validationResult){
        // validate the limit on the db
        MwResponseStatus mwResponseStatus;
        String cif = requestMetaData.getPrimaryCif();
        FundTransferRequest fundTransferRequest;
        FundTransferResponse fundTransferResponse;
        QRDealDetails qrDealDetails = qrDealsService.getQRDealDetails(cif, requestMetaData.getCountry());
        if(qrDealDetails == null){
            logAndThrow(FundTransferEventType.FUND_TRANSFER_CC_CALL, TransferErrorCode.FT_CC_NO_DEALS, requestMetaData);
        }
        log.info("Fund transfer CC QR Deals verified {}", htmlEscape(cif));
        BigDecimal maximumEligibleAmount = qrDealDetails.getTotalLimitAmount();
        BigDecimal utilizedAmount = qrDealDetails.getUtilizedLimitAmount();
        if(utilizedAmount == null){
            utilizedAmount = new BigDecimal("0");
        }
        BigDecimal balancedAmount = maximumEligibleAmount.subtract(utilizedAmount);
        BigDecimal requestedAmount  = requestDTO.getAmount();
        int result = balancedAmount.compareTo(requestedAmount);
        if(result == -1){
            logAndThrow(FundTransferEventType.FUND_TRANSFER_CC_CALL, TransferErrorCode.FT_CC_BALANCE_NOT_SUFFICIENT, requestMetaData);
        }
        log.info("Fund transfer CC Available Balance is verified {}", htmlEscape(cif));
        fundTransferRequest = prepareFundTransferRequestPayload(requestMetaData, requestDTO, cardDetailsDTO.getCurrency(),
                cardDetailsDTO.getSegment(), beneficiaryDto, validationResult);
        updateFundTransferRequest(beneficiaryDto, fundTransferRequest, requestDTO, cardDetailsDTO);
        log.info("Fund transfer CC calling MW {}", htmlEscape(cif));
        fundTransferResponse = fundTransferCCMWService.transfer(fundTransferRequest, requestMetaData);

        // Only if MW request is success, then update the utilized amount
        mwResponseStatus = fundTransferResponse.getResponseDto().getMwResponseStatus();
        if(isSuccessOrProcessing(fundTransferResponse)){
            utilizedAmount = utilizedAmount.add(requestedAmount);
            qrDealsService.updateQRDeals(cif, utilizedAmount);
            log.info("Fund transfer CC updated QR deals utilized amount {}", htmlEscape(cif));
            fundTransferRequest.setSourceOfFund(SOURCE_OF_FUND_CC);
            fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getTxnCurrency()));
            fundTransferRequest.setNotificationType(OTHER_ACCOUNT_TRANSACTION);
            fundTransferRequest.setFromAccount(cardDetailsDTO.getCardNoWithMasked());
            fundTransferRequest.setStatus(mwResponseStatus.getName());
            postTransactionService.performPostTransactionActivities(requestMetaData, fundTransferRequest);
        }
        return fundTransferResponse;
    }

    /**
     * Used to log and throws an exception with the proper error code
     * @param fundTransferEventType
     * @param errorCodeSet
     * @param requestMetaData
     */
    private void logAndThrow(FundTransferEventType fundTransferEventType, TransferErrorCode errorCodeSet, RequestMetaData requestMetaData){
        auditEventPublisher.publishFailureEvent(fundTransferEventType, requestMetaData,"",
                fundTransferEventType.name(), fundTransferEventType.getDescription(), fundTransferEventType.getDescription());
        GenericExceptionHandler.handleError(errorCodeSet,errorCodeSet.getErrorMessage());
    }

    /**
     *
     * @param currency
     * @return
     */
    private String getISOCurrency(String currency){
        String isoCurrency = null;
        List<CoreCurrencyDto> coreCurrencyDtos = maintenanceService.getAllCurrencies();
        for(CoreCurrencyDto coreCurrencyDto : coreCurrencyDtos){
            if(coreCurrencyDto.getCode().equalsIgnoreCase(currency)){
                isoCurrency = coreCurrencyDto.getIso();
                break;
            }
        }
        return isoCurrency;
    }

    private CustomerNotification populateCustomerNotification(String transactionRefNo, String currency, BigDecimal amount) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(currency);
        customerNotification.setTxnRef(transactionRefNo);
        return customerNotification;
    }

    /**
     * Used to update the request model specific to CC
     * @param beneficiaryDto
     * @param fundTransferRequest
     * @param requestDTO
     * @param cardDetailsDTO
     */
    private void updateFundTransferRequest(BeneficiaryDto beneficiaryDto, FundTransferRequest fundTransferRequest,
                                           FundTransferRequestDTO requestDTO, CardDetailsDTO cardDetailsDTO) {
        fundTransferRequest.setAcwthInst1(beneficiaryDto.getSwiftCode());
        fundTransferRequest.setAcwthInst2(beneficiaryDto.getBankName());
        Optional<Country> countryOptional = countryRepository.findByIsoCode2(beneficiaryDto.getBankCountry());
        if(countryOptional.isPresent()){
            fundTransferRequest.setAcwthInst5(countryOptional.get().getName());
        }
        fundTransferRequest.setCardNo(encryptionService.decrypt(cardDetailsDTO.getEncryptedCardNumber()));
        fundTransferRequest.setFromAccount(requestDTO.getCardNo());
        fundTransferRequest.setExpiryDate(cardDetailsDTO.getExpiryDate());
        fundTransferRequest.setSourceCurrency(requestDTO.getCurrency());
        fundTransferRequest.setSourceISOCurrency(getISOCurrency(fundTransferRequest.getSourceCurrency()));
        fundTransferRequest.setDestinationISOCurrency(getISOCurrency(fundTransferRequest.getDestinationCurrency()));
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
        //currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        BigDecimal amtToBePaidInSrcCurrency;
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency("AED");
        currencyRequest.setDealNumber(request.getDealNumber());
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  String currency, String branchCode, BeneficiaryDto beneficiaryDto, LimitValidatorResponse validationResult) {
    	String address3 = null;
    	if(StringUtils.isNotBlank(beneficiaryDto.getAddressLine2()) && StringUtils.isNotBlank(beneficiaryDto.getAddressLine3())){
    		address3 = StringUtils.left(beneficiaryDto.getAddressLine2().concat(SPACE_CHAR+beneficiaryDto.getAddressLine3()), maxLength);
    	} else if(StringUtils.isNotBlank(beneficiaryDto.getAddressLine2()) && StringUtils.isBlank(beneficiaryDto.getAddressLine3())){
    		address3 = StringUtils.left(beneficiaryDto.getAddressLine2(), maxLength);
    	} else if(StringUtils.isBlank(beneficiaryDto.getAddressLine2()) && StringUtils.isNotBlank(beneficiaryDto.getAddressLine3())){
    		address3 = StringUtils.left(beneficiaryDto.getAddressLine3(), maxLength);
    	}
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
                .beneficiaryFullName(StringUtils.isNotBlank(beneficiaryDto.getFullName()) && beneficiaryDto.getFullName().length() > maxLength ? StringUtils.left(beneficiaryDto.getFullName(), maxLength) : beneficiaryDto.getFullName())
                .beneficiaryAddressOne(StringUtils.isNotBlank(beneficiaryDto.getFullName()) && beneficiaryDto.getFullName().length() > maxLength ? beneficiaryDto.getFullName().substring(maxLength) : null)
                .beneficiaryAddressTwo(StringUtils.left(StringUtils.isBlank(beneficiaryDto.getAddressLine1()) ? beneficiaryDto.getBankCountry() : beneficiaryDto.getAddressLine1(), maxLength))
                .beneficiaryAddressThree(address3)
                .destinationCurrency(localCurrency)
                .awInstName(beneficiaryDto.getBankName())
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .beneficiaryAddressTwo(address)
                .transactionCode(LOCAL_TRANSACTION_CODE)
                .finalBene(request.getFinalBene())
                .dealNumber(request.getDealNumber())
                .dealRate(request.getDealRate())
                .txnCurrency(request.getTxnCurrency())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .acwthInst1(request.getAdditionalField()) //TODO Add For testing need to create new field to map
                .build();
    }

    
    private CardDetailsDTO getSelectedCreditCard(List<CardDetailsDTO> coreCardAccounts, String encryptedCardNo) {
        CardDetailsDTO cardDetailsDTO = null;
        String decryptedCardNo;
        String givenDecryptedCardNo = encryptionService.decrypt(encryptedCardNo);
        for(CardDetailsDTO currCardDetails : coreCardAccounts){
            decryptedCardNo = encryptionService.decrypt(currCardDetails.getEncryptedCardNumber());
            if(decryptedCardNo.equalsIgnoreCase(givenDecryptedCardNo)){
                cardDetailsDTO = currCardDetails;
                break;
            }
        }
        return cardDetailsDTO;
    }
    

    
    private boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }


    private String getTransferType(String txnCurrency){
        StringBuilder stringBuilder = new StringBuilder("Local ");
        if(AED.equalsIgnoreCase(txnCurrency) || txnCurrency == null){
            stringBuilder.append(AED);
        } else {
            stringBuilder.append(NON_AED);
        }
        return stringBuilder.toString();

    }
}
