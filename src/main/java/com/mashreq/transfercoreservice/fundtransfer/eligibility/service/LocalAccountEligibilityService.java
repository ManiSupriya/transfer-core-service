package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SEGMENT;
import static java.lang.Long.valueOf;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Override
	public void checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,
			UserDTO userDTO) {
        if(StringUtils.isNotBlank(request.getCardNo())){
        	if(isSMESegment(metaData)) {
        		GenericExceptionHandler.handleError(INVALID_SEGMENT, INVALID_SEGMENT.getErrorMessage());
        	}
        	executeCC(request, metaData, userDTO);
        } else {
        	executeNonCreditCard(request, metaData, userDTO);
        }
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

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);


        final AccountDetailsDTO fromAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(metaData.getPrimaryCif(), valueOf(request.getBeneficiaryId()), metaData);
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", AED);
        responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

        //Balance Validation
        final BigDecimal transferAmountInSrcCurrency = getAmountInSrcCurrency(request, fromAccountDetails);

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

    	final List<CardDetailsDTO> accountsFromCore = cardService.getCardsFromCore(requestMetaData.getPrimaryCif(), CardType.CC);
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);

        validationContext.add("from-account", getSelectedCreditCard(accountsFromCore, request.getCardNo()));

        validateBeneficiary(request, requestMetaData, validationContext);
        
        final BigDecimal limitUsageAmount = request.getAmount();
        limitValidatorFactory.getValidator(requestMetaData).validate(userDTO, request.getServiceType(), limitUsageAmount, requestMetaData, null);
        
    }

    /**
     * Get the beneficiary details and validate the details.
     * @param request
     * @param requestMetaData
     * @param validationContext
     * @return
     */
    private BeneficiaryDto validateBeneficiary(FundTransferEligibiltyRequestDTO request, RequestMetaData requestMetaData, ValidationContext validationContext) {

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(requestMetaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), requestMetaData);
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
