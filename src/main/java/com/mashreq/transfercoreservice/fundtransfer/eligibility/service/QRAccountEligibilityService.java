package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_BELONGS_TO_CIF;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.service.*;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.QRDealDetails;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CCBalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class QRAccountEligibilityService implements TransferEligibilityService {

	private static final String AED = "AED";

	private final BeneficiaryService beneficiaryService;
	private final AccountService accountService;
	private final MaintenanceService maintenanceService;
	private final BeneficiaryValidator beneficiaryValidator;
	private final LimitValidatorFactory limitValidatorFactory;
	private final CurrencyValidatorFactory currencyValidatorFactory;
	private final QuickRemitService quickRemitService;
	private final CCBalanceValidator ccBalanceValidator;
	private final QRDealsService qrDealsService;
	private final AuditEventPublisher auditEventPublisher;
	private final UserSessionCacheService userSessionCacheService;
	private final CardService cardService;
	private final EncryptionService encryptionService = new EncryptionService();

	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,
			UserDTO userDTO) {

		log.info("Quick remit eligibility initiated");
		
		if (isSMESegment(metaData) && StringUtils.isNotBlank(request.getCardNo())) {
			return EligibilityResponse.builder().status(FundsTransferEligibility.NOT_ELIGIBLE)
					.errorCode(INVALID_SEGMENT.getCustomErrorCode()).errorMessage(INVALID_SEGMENT.getErrorMessage())
					.build();
		}

		final ValidationContext validationContext = new ValidationContext();
		
		final BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdV2(metaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), metaData);
        
        List<CountryMasterDto> countryList = maintenanceService.getAllCountries("MOB", "AE", Boolean.TRUE);
        final Optional<CountryMasterDto> countryDto = countryList.stream()
                .filter(country -> country.getCode().equals(beneficiaryDto.getBankCountryISO()))
                .findAny();

        if (countryDto.isPresent()) {
        	validationContext.add("country", countryDto.get());
        }
        
        responseHandler(currencyValidatorFactory.getValidator(metaData).validate(request, metaData, validationContext));
        
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

		QRExchangeResponse response = quickRemitService.exchange(request, countryDto, metaData);
		if (!response.isAllowQR()) {
			return EligibilityResponse.builder().status(FundsTransferEligibility.NOT_ELIGIBLE).data(response).build();
		}

		if(StringUtils.isNotBlank(request.getCardNo())) {
			return validateQRCCFlow(request, response, metaData, beneficiaryDto, userDTO);
		}

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());
		final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore,
				request.getFromAccount());
		final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,
				new BigDecimal(response.getAccountCurrencyAmount()));

		limitValidatorFactory.getValidator(metaData).validate(
				userDTO,
				getServiceType() == ServiceType.QRT ? "QROC" : request.getServiceType(),
				limitUsageAmount, metaData, Long.valueOf(request.getBeneficiaryId()));
		updateExchangeRateDisplay(response);
		return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).data(response).build();
	}

	private EligibilityResponse validateQRCCFlow(FundTransferEligibiltyRequestDTO request, QRExchangeResponse response, RequestMetaData requestMetaData, BeneficiaryDto beneficiaryDto, UserDTO userDTO) {

		assertCardNumberBelongsToUser(request, requestMetaData);

		final List<CardDetailsDTO> accountsFromCore = cardService.getCardsFromCore(requestMetaData.getPrimaryCif(), CardType.CC);
		final ValidationContext validationContext = new ValidationContext();

		validationContext.add("account-details", accountsFromCore);
		validationContext.add("validate-from-account", Boolean.TRUE);

		final CardDetailsDTO selectedCreditCard = getSelectedCreditCard(accountsFromCore, request.getCardNo());

		validationContext.add("from-account", selectedCreditCard);

		final BigDecimal transferAmountInSrcCurrency = request.getAmount();

		String trxCurrency = StringUtils.isBlank(request.getTxnCurrency()) ? AED : request.getTxnCurrency();

		request.setTxnCurrency(trxCurrency);
		validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
		responseHandler(ccBalanceValidator.validate(request, requestMetaData, validationContext));

		final BigDecimal limitUsageAmount = transferAmountInSrcCurrency;
		limitValidatorFactory.getValidator(requestMetaData)
				.validate(userDTO, request.getServiceType(), limitUsageAmount, requestMetaData, null);

		QRDealDetails qrDealDetails = qrDealsService.getQRDealDetails(requestMetaData.getPrimaryCif(), beneficiaryDto.getBankCountryISO());
		if(qrDealDetails == null){
			logAndThrow(FundTransferEventType.FUND_TRANSFER_CC_CALL, TransferErrorCode.FT_CC_NO_DEALS, requestMetaData);
		}
		log.info("Fund transfer CC QR Deals verified {}", htmlEscape(requestMetaData.getPrimaryCif()));
		BigDecimal utilizedAmount = qrDealDetails.getUtilizedLimitAmount();
		if(utilizedAmount == null){
			utilizedAmount = new BigDecimal("0");
		}
		BigDecimal balancedAmount = qrDealDetails.getTotalLimitAmount().subtract(utilizedAmount);
		int result = balancedAmount.compareTo(request.getAmount());
		if(result == -1){
			logAndThrow(FundTransferEventType.FUND_TRANSFER_CC_CALL, TransferErrorCode.FT_CC_BALANCE_NOT_SUFFICIENT, requestMetaData);
		}
		updateExchangeRateDisplay(response);
		return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).data(response).build();
	}

	/**
	 * Generates exchange rate display string based on the power of the currency.
	 * logic applied, if exchangeRate is greater than one then 1 Transaction
	 * currency = X Account Currency else 1 Account Currency = Y Transaction
	 * currency where X -> Exchange rate and Y -> Reciprocal of exchange rate
	 * 
	 * @param response
	 * @return
	 */
	public void updateExchangeRateDisplay(QRExchangeResponse response) {
		StringBuilder builder = new StringBuilder("");
		BigDecimal exchangeRate = findAndUpdateExchangeRate(response);
		response.setExchangeRate(exchangeRate.toPlainString());
		if (exchangeRate.compareTo(BigDecimal.ONE) > 0) {
			builder =  builder.append("1 ").append(response.getTransactionCurrency()).append(" = ")
					.append(exchangeRate.setScale(5, RoundingMode.DOWN).toPlainString()).append(" ")
					.append(response.getAccountCurrency());
		} else {
			exchangeRate = findReciprocal(exchangeRate);
			builder = builder.append("1 ").append(response.getAccountCurrency()).append(" = ")
					.append(exchangeRate.setScale(5, RoundingMode.DOWN).toPlainString()).append(" ")
					.append(response.getTransactionCurrency());
		}
		response.setExchangeRateDisplay(builder.toString());
	}
	
	/**
	 * for updating exchange rate in right format
	 * @param response
	 * @return
	 */
	private BigDecimal findAndUpdateExchangeRate(QRExchangeResponse response) {
		BigDecimal debitAmount = new BigDecimal(response.getDebitAmountWithoutCharges());
		BigDecimal trxAmount = new BigDecimal(response.getTransactionAmount());
		log.info("debit amount without charges is {}, transaction amount is {} ",htmlEscape(debitAmount),htmlEscape(trxAmount));
		return debitAmount.divide(trxAmount,8,RoundingMode.HALF_UP);
	}

	private BigDecimal findReciprocal(BigDecimal exchangeRate) {
		return BigDecimal.ONE.divide(exchangeRate, 12, RoundingMode.HALF_DOWN);
	}
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.QRT;
	}

	private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
			final BigDecimal transferAmountInSrcCurrency) {
		return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
				? transferAmountInSrcCurrency
						: convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
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

	private void logAndThrow(FundTransferEventType fundTransferEventType, TransferErrorCode errorCodeSet, RequestMetaData requestMetaData){
		auditEventPublisher.publishFailureEvent(fundTransferEventType, requestMetaData,"",
				fundTransferEventType.name(), fundTransferEventType.getDescription(), fundTransferEventType.getDescription());
		GenericExceptionHandler.handleError(errorCodeSet,errorCodeSet.getErrorMessage());
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
}
