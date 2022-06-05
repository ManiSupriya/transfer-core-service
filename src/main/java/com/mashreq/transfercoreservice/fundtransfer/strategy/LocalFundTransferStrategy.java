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
import com.mashreq.transfercoreservice.common.CommonUtils;
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
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.*;
import static java.lang.Long.valueOf;

/**
 *
 */

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class LocalFundTransferStrategy implements FundTransferStrategy {

    @Data
    @AllArgsConstructor
    private class SelectedSourceOfFund{
        private String currency;
        private String segment;
        private String accountClass;
    }

    private static final String INDIVIDUAL_ACCOUNT = "I";
    private static final String SOURCE_OF_FUND_CC = "Credit Card";


    private static final int LOCAL_IBAN_LENGTH = 23;
    private static final String LOCAL_PRODUCT_ID = "DBLC";
    public static final String LOCAL_TRANSACTION_CODE = "15";
    public static final String SPACE_CHAR = " ";
    public static final int maxLength = 35;
    public static final String NON = "non-";
    private final IBANValidator ibanValidator;
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
    private final QRDealsService qrDealsService;
    private final CardService cardService;
    private final PostTransactionService postTransactionService;
    private final CCTransactionEligibilityValidator ccTrxValidator;
    @Value("${app.local.currency}")
    private String localCurrency;
    @Value("${app.local.address}")
    private String address;



    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO) {
        FundTransferResponse fundTransferResponse;
        responseHandler(ccTrxValidator.validate(request, requestMetaData));
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
    protected FundTransferResponse executeNonCreditCard(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {

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

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()), request.getJourneyVersion(), metadata);
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
        final CurrencyConversionDto currencyConversionDto = validateBalance(request, metadata, validationContext, fromAccountDetails, beneficiaryDto);

        //Limit Validation
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccountDetails, currencyConversionDto.getAccountCurrencyAmount());
        final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        String txnRefNo = validationResult.getTransactionRefNo();

        SelectedSourceOfFund selectedSourceOfFund = prepareSourceOfFundObj(fromAccountDetails.getCurrency(), fromAccountDetails.getBranchCode(), fromAccountDetails.getAccountType());
        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, selectedSourceOfFund, beneficiaryDto, validationResult, currencyConversionDto);
        log.info("Local Fund transfer initiated.......");

        final FundTransferResponse fundTransferResponse = processTransaction(metadata, txnRefNo, fundTransferRequest,request);

        handleSuccessfulTransaction(request, metadata, userDTO, validationResult, fundTransferRequest,
				fundTransferResponse);

        return prepareResponse(currencyConversionDto.getAccountCurrencyAmount(), limitUsageAmount, validationResult, txnRefNo, fundTransferResponse);

    }

    private SelectedSourceOfFund prepareSourceOfFundObj(String currency, String branchCode, String accountType) {
        return new SelectedSourceOfFund(currency,branchCode,accountType);
    }

    protected FundTransferResponse prepareResponse(final BigDecimal transferAmountInSrcCurrency,
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult, String txnRefNo,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .debitAmount(transferAmountInSrcCurrency).transactionRefNo(txnRefNo).build();
	}

	protected void handleSuccessfulTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
			UserDTO userDTO, final LimitValidatorResponse validationResult,
			final FundTransferRequest fundTransferRequest, final FundTransferResponse fundTransferResponse) {
		if(isSuccessOrProcessing(fundTransferResponse)){
            final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),
                    request.getAmount(),fundTransferRequest.getBeneficiaryFullName(),fundTransferRequest.getToAccount());
            notificationService.sendNotifications(customerNotification, LOCAL_FT, metadata,userDTO);
            fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getTxnCurrency()));
            fundTransferRequest.setNotificationType(OTHER_ACCOUNT_TRANSACTION);
            fundTransferRequest.setStatus(fundTransferResponse.getResponseDto().getMwResponseStatus().getName());
            postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest, request);
        }
	}

	protected FundTransferResponse processTransaction(RequestMetaData metadata, String txnRefNo,
			final FundTransferRequest fundTransferRequest,FundTransferRequestDTO request) {
		final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata, txnRefNo);
		return fundTransferResponse;
	}

	protected CurrencyConversionDto validateBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validationContext, final AccountDetailsDTO fromAccountDetails,
			final BeneficiaryDto beneficiaryDto) {
		final CurrencyConversionDto currencyConversionDto = getAmountInSrcCurrency(request, fromAccountDetails);
        validationContext.add("transfer-amount-in-source-currency", currencyConversionDto.getAccountCurrencyAmount());
        responseHandler(balanceValidator.validate(request, metadata, validationContext));
		return currencyConversionDto;
	}

    /**
     * Method is used to initiate the Fund transfer for the Credit card
     * @param request
     * @param requestMetaData
     * @param userDTO
     * @return
     */
    protected FundTransferResponse executeCC(FundTransferRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO){

        FundTransferResponse fundTransferResponse;

        final List<CardDetailsDTO> accountsFromCore = cardService.getCardsFromCore(requestMetaData.getPrimaryCif(), CardType.CC);
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);

        final CardDetailsDTO selectedCreditCard = getSelectedCreditCard(accountsFromCore, request.getCardNo());
        validationContext.add("from-account", selectedCreditCard);

        final BeneficiaryDto beneficiaryDto = validateBeneficiary(request, requestMetaData, validationContext);

        final BigDecimal transferAmountInSrcCurrency = request.getAmount();

        String trxCurrency = StringUtils.isBlank(request.getTxnCurrency()) ? localCurrency
                : request.getTxnCurrency();

        request.setTxnCurrency(trxCurrency);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(ccBalanceValidator.validate(request, requestMetaData, validationContext));

        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final BigDecimal limitUsageAmount = transferAmountInSrcCurrency;
        final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, requestMetaData, bendId);
        String txnRefNo = validationResult.getTransactionRefNo();

        fundTransferResponse = processCreditCardTransfer(request, requestMetaData, selectedCreditCard, beneficiaryDto, validationResult);

        if(isSuccessOrProcessing(fundTransferResponse)){
            final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),request.getAmount(),"","");
            notificationService.sendNotifications(customerNotification, LOCAL_FT_CC, requestMetaData,userDTO);
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

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(requestMetaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), request.getJourneyVersion(), requestMetaData);
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
        QRDealDetails qrDealDetails = qrDealsService.getQRDealDetails(cif, beneficiaryDto.getBankCountryISO());
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

        SelectedSourceOfFund selectedSourceOfFund = prepareSourceOfFundObj(cardDetailsDTO.getCurrency(),cardDetailsDTO.getSegment(), null);
        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setAccountCurrencyAmount(requestedAmount);

        log.info("Fund transfer CC Available Balance is verified {}", htmlEscape(cif));
        fundTransferRequest = prepareFundTransferRequestPayload(requestMetaData, requestDTO, selectedSourceOfFund, beneficiaryDto, validationResult, currencyConversionDto);
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
            postTransactionService.performPostTransactionActivities(requestMetaData, fundTransferRequest, requestDTO);
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

    protected CustomerNotification populateCustomerNotification(String transactionRefNo, String currency, BigDecimal amount, String beneficiaryName, String creditAccount) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(currency);
        customerNotification.setTxnRef(transactionRefNo);
        customerNotification.setBeneficiaryName(beneficiaryName);
        customerNotification.setCreditAccount(creditAccount);
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
        return localCurrency.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(sourceAccountDetailsDTO, transferAmountInSrcCurrency);
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

    protected CurrencyConversionDto getAmountInSrcCurrency(FundTransferRequestDTO request, AccountDetailsDTO sourceAccountDetailsDTO) {
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency(localCurrency);
        currencyRequest.setDealNumber(request.getDealNumber());
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto currencyConversionDto =  maintenanceService.convertBetweenCurrencies(currencyRequest);
        currencyConversionDto.setExchangeRateDisplayTxt(CommonUtils.generateDisplayString(currencyConversionDto, currencyRequest));
        return currencyConversionDto;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  SelectedSourceOfFund selectedSourceOfFund, BeneficiaryDto beneficiaryDto,
                                                                  LimitValidatorResponse validationResult, CurrencyConversionDto currencyConversionDto) {
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
                .srcCcyAmt(currencyConversionDto.getAccountCurrencyAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(beneficiaryDto.getAccountNumber())
                .purposeCode(request.getPurposeCode())
                .purposeDesc(request.getPurposeDesc())
                .chargeBearer(request.getChargeBearer())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(selectedSourceOfFund.getCurrency())
                .sourceBranchCode(selectedSourceOfFund.getSegment())
                .beneficiaryFullName(StringUtils.isNotBlank(beneficiaryDto.getFullName()) && beneficiaryDto.getFullName().length() > maxLength ? StringUtils.left(beneficiaryDto.getFullName(), maxLength) : beneficiaryDto.getFullName())
                .beneficiaryAddressOne(StringUtils.isNotBlank(beneficiaryDto.getFullName()) && beneficiaryDto.getFullName().length() > maxLength ? beneficiaryDto.getFullName().substring(maxLength) : null)
                .beneficiaryAddressTwo(address)
                .beneficiaryAddressThree(address3)
                .destinationCurrency(localCurrency)
                .awInstName(beneficiaryDto.getBankName())
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .beneficiaryBankCountry(address)
                .transactionCode(LOCAL_TRANSACTION_CODE)
                .finalBene(request.getFinalBene())
                .dealNumber(request.getDealNumber())
                .dealRate(request.getDealRate())
                .txnCurrency(request.getTxnCurrency())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .acwthInst1(request.getAdditionalField()) //TODO Add For testing need to create new field to map
                .serviceType(request.getServiceType())
                .paymentNote(request.getPaymentNote())
                .accountClass(selectedSourceOfFund.getAccountClass())
                .exchangeRateDisplayTxt(currencyConversionDto.getExchangeRateDisplayTxt())
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


    protected String getTransferType(String txnCurrency){
        StringBuilder stringBuilder = new StringBuilder("Local ");
        if(localCurrency.equalsIgnoreCase(txnCurrency) || txnCurrency == null){
            stringBuilder.append(localCurrency);
        } else {
            stringBuilder.append(NON).append(localCurrency);
        }
        return stringBuilder.toString();

    }
}
