package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.LOCAL_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CURRENCY_IS_INVALID;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.QRT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("currencyValidatorEligibility")
@RequiredArgsConstructor
public class CurrencyValidator implements ICurrencyValidator {

    private final AsyncUserEventPublisher auditEventPublisher;
    private final MaintenanceService maintenanceService;
    private static final String INTERNATIONAL = "INTERNATIONAL";
    
    @Value("${app.local.currency}")
    private String localCurrency;

    
    @Override
    public ValidationResult validate(FundTransferEligibiltyRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating transaction currency {} for service type [ {} ] ", htmlEscape(request.getTxnCurrency()), htmlEscape(request.getServiceType()));
        
        if(INFT.getName().equals(request.getServiceType())) {
        	boolean isValidCurrency = maintenanceService.getAllCurrencies()
        			.stream()
        			.filter(Objects::nonNull)
        			.filter(currency -> INTERNATIONAL.equals(currency.getFunction()))
        			.map(CoreCurrencyDto::getCode)
        			.anyMatch(request.getTxnCurrency()::equals);
        	if(!isValidCurrency) {
        		log.error("Transaction Currency is not eligigle for service type [ {} ]  ", htmlEscape(request.getServiceType()));
            	auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
            			CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null );
        	}
        	return ValidationResult.builder().success(isValidCurrency).transferErrorCode(CURRENCY_IS_INVALID).build();

        }
        
        if(QRT.getName().equals(request.getServiceType())) {
        	final CountryMasterDto countryMasterDto = context.get("country", CountryMasterDto.class);

            if (null != countryMasterDto) {
                if (request.getTxnCurrency().equalsIgnoreCase(countryMasterDto.getNativeCurrency())) {
                	return ValidationResult.builder().success(true).build();
                }
            }
    		
            log.error("Transaction Currency is not eligigle for service type [ {} ]  ", htmlEscape(request.getServiceType()));
        	auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
        			CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null );
        	
        	return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();

        }       
        
        String requestedCurrency = request.getCurrency();
        log.info("Requested currency [ {} ] service type [ {} ] ", htmlEscape(requestedCurrency), htmlEscape(request.getServiceType()));

        AccountDetailsDTO fromAccount = null;
        BeneficiaryDto beneficiaryDto = null;
        AccountDetailsDTO toAccount = null;
        if(null != context) {
        	fromAccount = context.get("from-account", AccountDetailsDTO.class);
        	beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
        	toAccount = context.get("to-account", AccountDetailsDTO.class);
        }
        
        if(WAMA.getName().equals(request.getServiceType()) ) {
            if (beneficiaryDto != null && fromAccount!=null && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), null)) {
                log.error("Beneficiary Currency and Requested Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                        CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null );
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }
        }

        if(WYMA.getName().equals(request.getServiceType()) ) {
            if ((fromAccount != null && toAccount != null) && ((!requestedCurrency.equals(fromAccount.getCurrency())) || requestedCurrency.equals(toAccount.getCurrency()))) {
                log.error("To Account Currency and Requested Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), ACCOUNT_CURRENCY_MISMATCH.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH).build();
            }
        }
        
        if(LOCAL.getName().equals(request.getServiceType()) ) {
            if (!(localCurrency.equals(request.getTxnCurrency()))) {
                log.error("Transaction Currency and local Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                		LOCAL_CURRENCY_MISMATCH.getCustomErrorCode(), LOCAL_CURRENCY_MISMATCH.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.LOCAL_CURRENCY_MISMATCH).build();
            }
        }

        log.info("Currency Validating successful service type [ {} ] ", htmlEscape(request.getServiceType()));
        auditEventPublisher.publishSuccessEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }

    private boolean isReqCurrencyValid(String requestedCurrency, String fromAccCurrency, String toCurrency) {
        return requestedCurrency.equals(fromAccCurrency) || requestedCurrency.equals(toCurrency);
    }
}
