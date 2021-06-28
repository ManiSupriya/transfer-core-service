package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class INFTAccountEligibilityService implements TransferEligibilityService {

    private static final String INTERNATIONAL_VALIDATION_TYPE = "international";
    private final AccountService accountService;
    private final BeneficiaryValidator beneficiaryValidator;
    private final MaintenanceService maintenanceService;
    private final BeneficiaryService beneficiaryService;
    private final CurrencyValidatorFactory currencyValidatorFactory;
    private final LimitValidatorFactory limitValidatorFactory;

    
    @Override
	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,UserDTO userDTO) {
    	log.info("INFT transfer eligibility validation started");
    	
    	responseHandler(currencyValidatorFactory.getValidator(metaData).validate(request, metaData));
    	
		final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());

		final ValidationContext validationContext = new ValidationContext();

		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		if (request.getBeneRequiredFields() != null && ((request.getBeneRequiredFields().getMissingFields() != null
				&& !request.getBeneRequiredFields().getMissingFields().isEmpty())
				|| (request.getBeneRequiredFields().getIncorrectFields() != null
						&& !request.getBeneRequiredFields().getIncorrectFields().isEmpty()))) {
			log.info("Update missing beneficiary details");
			request.getBeneRequiredFields().setNewVersion(true);
			beneficiaryDto = beneficiaryService.getUpdate(request.getBeneRequiredFields(),
					Long.valueOf(request.getBeneficiaryId()), metaData, INTERNATIONAL_VALIDATION_TYPE);
		} else {
			beneficiaryDto = beneficiaryService.getByIdV2(metaData.getPrimaryCif(),
					Long.valueOf(request.getBeneficiaryId()), metaData);
		}

		validationContext.add("beneficiary-dto", beneficiaryDto);
		responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

		final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore,
				request.getFromAccount());

		final BigDecimal transferAmountInSrcCurrency = getAmountInSrcCurrency(request, sourceAccountDetailsDTO);

		// Limit Validation
		Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId()) ? Long.parseLong(request.getBeneficiaryId())
				: null;
		final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,
				transferAmountInSrcCurrency);

		limitValidatorFactory.getValidator(metaData).validate(userDTO, request.getServiceType(),
				limitUsageAmount, metaData, bendId);
		log.info("INFT transfer eligibility validation successfully finished");
        return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).build();
    }

	@Override
	public ServiceType getServiceType() {
		return ServiceType.INFT;
	}

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
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

    private BigDecimal getAmountInSrcCurrency(FundTransferEligibiltyRequestDTO request, AccountDetailsDTO sourceAccountDetailsDTO) {
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
}
