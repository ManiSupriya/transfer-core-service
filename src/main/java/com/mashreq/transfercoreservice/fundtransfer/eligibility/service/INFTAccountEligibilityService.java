package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import java.math.BigDecimal;
import java.util.List;

import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.ILimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitManagementConfig;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorRequest;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorImpl;
import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

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
    private final RuleSpecificValidatorImpl CountrySpecificValidatorProvider;
    private final LimitManagementConfig limitManagementConfig;


    @Value("${app.local.currency}")
    private String localCurrency;

    @Override
	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,UserDTO userDTO) {
    	log.info("INFT transfer eligibility validation started");
    	
        final ValidationContext validationContext = new ValidationContext();

		BeneficiaryDto beneficiaryDto;
		if (request.getBeneRequiredFields() != null && ((request.getBeneRequiredFields().getMissingFields() != null
				&& !request.getBeneRequiredFields().getMissingFields().isEmpty())
				|| (request.getBeneRequiredFields().getIncorrectFields() != null
						&& !request.getBeneRequiredFields().getIncorrectFields().isEmpty()))) {
			log.info("Update missing beneficiary details");
			request.getBeneRequiredFields().setNewVersion(true);
			beneficiaryDto = beneficiaryService.getUpdate(request.getBeneRequiredFields(),
					Long.valueOf(request.getBeneficiaryId()), "V2", metaData, INTERNATIONAL_VALIDATION_TYPE);
		} else {
			beneficiaryDto = beneficiaryService.getByIdWithoutValidation(metaData.getPrimaryCif(),
					Long.valueOf(request.getBeneficiaryId()), "V2", metaData);
		}

		validationContext.add("beneficiary-dto", beneficiaryDto);
		responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

        Validator<RuleSpecificValidatorRequest> countrySpecificValidator =
                CountrySpecificValidatorProvider.getCcyValidator(
                        request.getCurrency(),
                        "INFT"
                );

        if (countrySpecificValidator != null) {
            RuleSpecificValidatorRequest validationRequest =
                    RuleSpecificValidatorRequest.builder()
                            .sourceAccountCurrency(request.getCurrency())
                            .txnCurrency(request.getTxnCurrency()).build();
            responseHandler(countrySpecificValidator.validate(validationRequest, metaData, validationContext));
        }

        final AccountDetailsDTO sourceAccountDetailsDTO = accountService.getAccountDetailsFromCache(request.getFromAccount(), metaData);
        
        validationContext.add("from-account", sourceAccountDetailsDTO);
        responseHandler(currencyValidatorFactory.getValidator(metaData).validate(request, metaData, validationContext));

		final BigDecimal transferAmountInSrcCurrency = getAmountInSrcCurrency(request, sourceAccountDetailsDTO);

		// Limit Validation 
		final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,
				transferAmountInSrcCurrency);

        List<String> allowedChannels = limitManagementConfig.getCountries().get(metaData.getCountry());

        ILimitValidator limitValidator = limitValidatorFactory.getValidator(metaData);
        if(!CollectionUtils.isEmpty(allowedChannels) && allowedChannels.contains(metaData.getChannel())) {
            LimitValidatorResponse  limitValidatorResponse = limitValidator
                    .validateAvailableLimits(userDTO, request.getServiceType(),
                            limitUsageAmount, metaData, beneficiaryDto.getId());
            return EligibilityResponse.builder().status(FundsTransferEligibility.valueOf(limitValidatorResponse.getVerificationType())).data(limitValidatorResponse).build();
        } else {
            limitValidator.validate(userDTO, request.getServiceType(),
                    limitUsageAmount, metaData, beneficiaryDto.getId());
            log.info("INFT transfer eligibility validation successfully finished");
            return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).build();
        }


    }

	@Override
	public ServiceType getServiceType() {
		return ServiceType.INFT;
	}

    private BigDecimal convertAmountInLocalCurrency(final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setTransactionCurrency(localCurrency);

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
        return localCurrency.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(sourceAccountDetailsDTO, transferAmountInSrcCurrency);
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
