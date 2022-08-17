package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CURRENCY_IS_INVALID;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.LOCAL_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.QRT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.webcore.dto.response.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!egypt")
@Slf4j
@Component("currencyValidatorEligibility")
@RequiredArgsConstructor
public class CurrencyValidator implements ICurrencyValidator {

    final AsyncUserEventPublisher auditEventPublisher;
    private final MobCommonClient mobCommonClient;
    private final String function = "code";
    
    @Value("${app.local.currency}")
    private String localCurrency;

    @Override
    public ValidationResult validate(FundTransferEligibiltyRequestDTO request, RequestMetaData metadata, ValidationContext context) {

    	log.info("Validating transaction currency {} for service type [ {} ] ", htmlEscape(request.getTxnCurrency()), htmlEscape(request.getServiceType()));
        CoreCurrencyDto transferCurrency;
        if(INFT.getName().equals(request.getServiceType())) {
        	transferCurrency = fetchAllTransferSupportedCurrencies(request.getTxnCurrency(),metadata);
        	if(transferCurrency == null) {
        		log.error("Not able to find requested currency :: {} in INFTALL currency list", htmlEscape(request.getTxnCurrency()));
        		return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
        	}
        	if(!transferCurrency.isSwiftTransferEnabled()) {
        		log.error("Swift transfer is disabled for this currency");
        		logFailure(request, metadata);
        	}
        	return ValidationResult.builder().success(transferCurrency.isSwiftTransferEnabled()).transferErrorCode(CURRENCY_IS_INVALID).build();
        }
        
        if(QRT.getName().equals(request.getServiceType())) {
            transferCurrency = fetchAllTransferSupportedCurrencies(request.getTxnCurrency(),metadata);
        	if(transferCurrency == null) {
        		log.error("Not able to find requested currency :: {} in INFTALL currency list", htmlEscape(request.getServiceType()));
        		return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
        	}
        	if(!transferCurrency.isQuickRemitEnabled()) {
        		logFailure(request, metadata);
        	}
        	return ValidationResult.builder().success(transferCurrency.isQuickRemitEnabled()).transferErrorCode(CURRENCY_IS_INVALID).build();

        }       
        
        String requestedCurrency = request.getTxnCurrency();
        log.info("Requested currency [ {} ] service type [ {} ] ", htmlEscape(requestedCurrency), htmlEscape(request.getServiceType()));

        AccountDetailsDTO fromAccount = null;
        BeneficiaryDto beneficiaryDto = null;
        AccountDetailsDTO toAccount = null;
        if(Objects.nonNull(context)) {
        	fromAccount = context.get("from-account", AccountDetailsDTO.class);
        	beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
        	toAccount = context.get("to-account", AccountDetailsDTO.class);
        	
        }
        
        if(WAMA.getName().equals(request.getServiceType()) ) {
            SearchAccountDto wamaToAccount = null;
            if(Objects.nonNull(context)) {
                wamaToAccount = context.get("credit-account-details", SearchAccountDto.class);
            }
        	if (Objects.nonNull(beneficiaryDto) && Objects.nonNull(fromAccount) && Objects.nonNull(wamaToAccount)
                    && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), wamaToAccount.getCurrency())) {
            	log.error("From account currency {} and to account currency {}", fromAccount.getCurrency());
                log.error("Beneficiary Currency [{}] and Requested Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                        CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null );
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }
        }

        if(WYMA.getName().equals(request.getServiceType()) ) {
            if ((Objects.nonNull(fromAccount) && Objects.nonNull(toAccount)) &&
            		!(requestedCurrency.equals(fromAccount.getCurrency()) || requestedCurrency.equals(toAccount.getCurrency()))) {
            	log.error("From account currency {} and to account currency {}", fromAccount.getCurrency(), toAccount.getCurrency());
            	log.error("To Account Currency and Requested Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), ACCOUNT_CURRENCY_MISMATCH.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH).build();
            }
        }
        
        if(LOCAL.getName().equals(request.getServiceType()) ) {
            if (!(localCurrency.equals(request.getTxnCurrency()))) {
                log.error("Transaction Currency [{}] and local Currency [{}] does not match for service type [ {} ]  ", 
                		htmlEscape(request.getTxnCurrency()), htmlEscape(localCurrency), htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                		LOCAL_CURRENCY_MISMATCH.getCustomErrorCode(), LOCAL_CURRENCY_MISMATCH.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.LOCAL_CURRENCY_MISMATCH).build();
            }
        }

        log.info("Currency Validating successful service type [ {} ] ", htmlEscape(request.getServiceType()));
        auditEventPublisher.publishSuccessEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }

	private void logFailure(FundTransferEligibiltyRequestDTO request, RequestMetaData metadata) {
		log.error("Transaction Currency is not eligigle for service type [ {} ]  ", htmlEscape(request.getServiceType()));
		auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
				CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null );
	}

    private CoreCurrencyDto fetchAllTransferSupportedCurrencies(String txnCurrency, RequestMetaData metadata) {
    	Response<List<CoreCurrencyDto>> transferCurrencies = mobCommonClient.getTransferCurrencies(function,metadata.getCountry(),txnCurrency);
    	if(transferCurrencies != null && transferCurrencies.hasData() && !transferCurrencies.getData().isEmpty()) {
    		return transferCurrencies.getData().get(0);
    	}
		return null;
	}

	private boolean isReqCurrencyValid(String requestedCurrency, String fromAccCurrency, String toCurrency) {
        return requestedCurrency.equals(fromAccCurrency) || requestedCurrency.equals(toCurrency);
    }
}
