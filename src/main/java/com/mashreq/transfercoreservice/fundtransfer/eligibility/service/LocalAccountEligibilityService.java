package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.ExceptionUtils;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.QRDealDetails;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CCBalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_BELONGS_TO_CIF;
import static java.lang.Long.valueOf;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalAccountEligibilityService implements TransferEligibilityService{
	
    private static final String AED = "AED";
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;
    private final LimitValidatorFactory limitValidatorFactory;
    private final CardService cardService;
    private final MaintenanceService maintenanceService;
    private final EncryptionService encryptionService = new EncryptionService();
    private final CurrencyValidatorFactory currencyValidatorFactory;
    private final CCBalanceValidator ccBalanceValidator;
    private final QRDealsService qrDealsService;
    private final AuditEventPublisher auditEventPublisher;
    private final UserSessionCacheService userSessionCacheService;

    @Override
	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,
			UserDTO userDTO) {
    	log.info("Local transfer eligibility validation started");
		if (StringUtils.isNotBlank(request.getCardNo())) {
			if (isSMESegment(metaData)) {
				return EligibilityResponse.builder().status(FundsTransferEligibility.NOT_ELIGIBLE)
						.errorCode(INVALID_SEGMENT.getCustomErrorCode()).errorMessage(INVALID_SEGMENT.getErrorMessage())
						.build();
			}
			executeCC(request, metaData, userDTO);
		} else {
			executeNonCreditCard(request, metaData, userDTO);
		}
		log.info("Local transfer eligibility validation successfully finished");
		return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).build();
	}

	@Override
	public ServiceType getServiceType() {
		return ServiceType.LOCAL;
	}
    

    /**
     * Method is used to initiate the fund transfer request except CC
     * @param request
     * @param metaData
     * @param userDTO
     * @return
     */
    private void executeNonCreditCard(FundTransferEligibiltyRequestDTO request, RequestMetaData metaData, UserDTO userDTO) {

    	responseHandler(currencyValidatorFactory.getValidator(metaData).validate(request, metaData));
    	
        final AccountDetailsDTO fromAccountDetails = accountService.getAccountDetailsFromCache(request.getFromAccount(), metaData);
        final ValidationContext validationContext = new ValidationContext();

        validationContext.add("validate-from-account", Boolean.TRUE);
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(metaData.getPrimaryCif(), valueOf(request.getBeneficiaryId()), "V2", metaData);
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", AED);
        responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

        //Balance Validation
        final BigDecimal transferAmountInSrcCurrency = AED.equals(fromAccountDetails.getCurrency()) ?
        		request.getAmount() : 
        			getAmountInSrcCurrency(request, fromAccountDetails);

        //Limit Validation
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccountDetails, transferAmountInSrcCurrency);
        limitValidatorFactory.getValidator(metaData).validate(userDTO, request.getServiceType(), limitUsageAmount, metaData, bendId);

    }

    /**
     * Method is used to initiate the Fund transfer for the Credit card
     * @param request
     * @param requestMetaData
     * @param userDTO
     * @return
     */
    private void executeCC(FundTransferEligibiltyRequestDTO request, RequestMetaData requestMetaData, UserDTO userDTO){

        responseHandler(currencyValidatorFactory.getValidator(requestMetaData).validate(request, requestMetaData));

        assertCardNumberBelongsToUser(request, requestMetaData);

        final CardDetailsDTO selectedCreditCard = cardService.getCardDetailsFromCache(request.getCardNo(), requestMetaData);

        final ValidationContext validationContext = new ValidationContext();

        validationContext.add("validate-from-account", Boolean.TRUE);

        validationContext.add("from-account", selectedCreditCard);

        BeneficiaryDto beneficiaryDto = validateBeneficiary(request, requestMetaData, validationContext);

        final BigDecimal transferAmountInSrcCurrency = request.getAmount();

        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(ccBalanceValidator.validate(request, requestMetaData, validationContext));

        final BigDecimal limitUsageAmount = transferAmountInSrcCurrency;
        limitValidatorFactory.getValidator(requestMetaData)
                .validate(userDTO, request.getServiceType(), limitUsageAmount, requestMetaData, null);
        //Credit card transaction limit validation is happening here, it is getting updated on a monthly basis and inserted into qr_deals_details table.
        QRDealDetails qrDealDetails = qrDealsService.getQRDealDetails(requestMetaData.getPrimaryCif(), beneficiaryDto.getBankCountryISO());
        if(qrDealDetails == null){
            logAndThrow(FundTransferEventType.FUND_TRANSFER_CC_CALL, TransferErrorCode.FT_CC_NO_DEALS, requestMetaData);
        }
        log.info("Fund transfer CC QR Deals verified {}", htmlEscape(requestMetaData.getPrimaryCif()));
        BigDecimal utilizedAmount = qrDealDetails.getUtilizedLimitAmount();
        if(utilizedAmount == null){
            utilizedAmount = BigDecimal.ZERO;
        }
        BigDecimal balancedAmount = qrDealDetails.getTotalLimitAmount().subtract(utilizedAmount);
        int result = balancedAmount.compareTo(request.getAmount());
        if(result < 0){
            logAndThrow(FundTransferEventType.FUND_TRANSFER_CC_CALL, TransferErrorCode.FT_CC_BALANCE_NOT_SUFFICIENT, requestMetaData);
        }
    }

    private void assertCardNumberBelongsToUser(FundTransferEligibiltyRequestDTO fundOrderCreateRequest, RequestMetaData metaData) {
        if (userSessionCacheService.isCardNumberBelongsToCif(fundOrderCreateRequest.getCardNo(), metaData.getUserCacheKey())) {
            log.info("setting payment mode to card");
            String cardNumber = encryptionService.decrypt(fundOrderCreateRequest.getCardNo());
            if (org.springframework.util.StringUtils.isEmpty(cardNumber)) {
                log.info("card number is empty");
                auditEventPublisher.publishFailureEvent(ACCOUNT_BELONGS_TO_CIF, metaData, null,
                        ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), null);
                GenericExceptionHandler.handleError(ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF, ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage());
            }
            auditEventPublisher.publishSuccessEvent(ACCOUNT_BELONGS_TO_CIF, metaData, "card belongs to the cif");
            return;
        }
    }

    /**
     * Get the beneficiary details and validate the details.
     * @param request
     * @param requestMetaData
     * @param validationContext
     * @return
     */
    private BeneficiaryDto validateBeneficiary(FundTransferEligibiltyRequestDTO request, RequestMetaData requestMetaData, ValidationContext validationContext) {

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(requestMetaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), "V2", requestMetaData);
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", AED);
        responseHandler(beneficiaryValidator.validate(request, requestMetaData, validationContext));

        return beneficiaryDto;
    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        return AED.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferEligibiltyRequestDTO request, AccountDetailsDTO sourceAccountDetailsDTO) {
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

    private void logAndThrow(FundTransferEventType fundTransferEventType, TransferErrorCode errorCodeSet, RequestMetaData requestMetaData){
        auditEventPublisher.publishFailureEvent(fundTransferEventType, requestMetaData,"",
                fundTransferEventType.name(), fundTransferEventType.getDescription(), fundTransferEventType.getDescription());
        throw ExceptionUtils.genericException(errorCodeSet);
    }
}
