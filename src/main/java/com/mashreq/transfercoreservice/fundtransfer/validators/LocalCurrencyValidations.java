package com.mashreq.transfercoreservice.fundtransfer.validators;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CURRENCY_IS_INVALID;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.webcore.dto.response.Response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LocalCurrencyValidations {

    private final String localCurrency;
    private final AsyncUserEventPublisher auditEventPublisher;
    private final MobCommonClient mobCommonClient;
    private final String function = "code";

    public LocalCurrencyValidations(@Value("${app.local.currency}") String localCurrency, 
    		AsyncUserEventPublisher auditEventPublisher, MobCommonClient mobCommonClient) {
        this.localCurrency = localCurrency;
        this.auditEventPublisher = auditEventPublisher;
		this.mobCommonClient = mobCommonClient;
    }

    public ValidationResult performLocalCurrencyChecks(LocalCurrencyValidationRequest request) {
        log.info("Local account and currency validation for service type [{}] and transaction currency [{}]",
                htmlEscape(request.getServiceType()), htmlEscape(request.getTransactionCurrency()));
        AccountDetailsDTO fromAccount = request.getValidationContext().get("from-account", AccountDetailsDTO.class);

        String requestedCurrency = request.getTransactionCurrency();
        
        if(StringUtils.isEmpty(requestedCurrency) || StringUtils.isEmpty(fromAccount.getCurrency())) {
        	
        	log.error("Invalid request. Transaction currency and from account currency cannot be empty");
        	auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
        			CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(),
                    null);
        	return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
        }
        
        TransferErrorCode transferErrorCode = CURRENCY_IS_INVALID;
        boolean transferEligible = true;
        
        //STORY 85098  WYMA, WAMA & LOCAL If source account is EGP, transaction should be in EGP currency
        //STORY 91896 WYMA, WAMA, LOCAL - If source account is any FCY, transaction currency should be same or EGP currency
        if(isWithinRegionTransfer(request)) {
        	if(fromAccount.getCurrency().equals(localCurrency)) {
        		if(!requestedCurrency.equals(localCurrency)) {
	                log.error("Transaction currency [{}] not allowed for transaction type [{}] from [{}] currency account",
	                        requestedCurrency, request.getServiceType(), fromAccount.getCurrency());
	                transferEligible = false;
	                transferErrorCode = CURRENCY_IS_INVALID;
        		}
        	} else {
        		if(!(requestedCurrency.equals(localCurrency) || requestedCurrency.equals(fromAccount.getCurrency()))) {
        			log.error("Transaction currency [{}] not allowed for transaction type [{}] from [{}] currency account",
	                        requestedCurrency, request.getServiceType(), fromAccount.getCurrency());
        			transferEligible = false;
        			transferErrorCode = CURRENCY_IS_INVALID;
        		}
        	}
        }
        
        //STORY 85098 WYMA or WAMA - If source account is EGP, destination should be EGP account
        // STORY 91896 WYMA, WAMA - If source account is any FCY, destination account currency should be same currency or EGP
        if(WYMA.getName().equals(request.getServiceType()) || WAMA.getName().equals(request.getServiceType())) {
        	
        	String toAccountCurrency = WYMA.getName().equals(request.getServiceType()) ? 
        			request.getValidationContext().get("to-account", AccountDetailsDTO.class).getCurrency() :
        				request.getValidationContext().get("credit-account-details", SearchAccountDto.class).getCurrency();
        	
        	if(fromAccount.getCurrency().equals(localCurrency)) {
        		if(!toAccountCurrency.equals(localCurrency)) {
	                log.error("Destination account currency [{}] not allowed for transaction type [{}] from [{}] currency account",
	                		toAccountCurrency, request.getServiceType(), fromAccount.getCurrency());
	                transferEligible = false;
	                transferErrorCode = CURRENCY_IS_INVALID;
        		}
        	} else {
        		if(!(toAccountCurrency.equals(localCurrency) || toAccountCurrency.equals(fromAccount.getCurrency()))) {
        			log.error("Destination account currency [{}] not allowed for transaction type [{}] from [{}] currency account",
        					toAccountCurrency, request.getServiceType(), fromAccount.getCurrency());
        			transferEligible = false;
        			transferErrorCode = CURRENCY_IS_INVALID;
        		}
        	}
        }
        
        if(!transferEligible) {
        	 auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
        			 transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), null);
             return ValidationResult.builder().success(false).transferErrorCode(transferErrorCode).build();
        }
        
        // INFT - Source account cannot be EGP
        // INFT - Transaction currency cannot be EGP
        // STORY 91896 INFT - Transaction currency should be same as source account currency
        if (INFT.getName().equals(request.getServiceType())) {
        	
        	// INFT - Transaction currency cannot be EGP
        	if (localCurrency.equals(requestedCurrency)) {
                log.error("For transation type [{}], transaction cannot be in local currency [{}]",
                        request.getServiceType(), localCurrency);
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }

        	// INFT - Source account cannot be EGP
            if (localCurrency.equals(fromAccount.getCurrency())) {
                log.error("For transation type [{}], source account cannot be a local currency [{}] account",
                        request.getServiceType(), localCurrency);
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), ACCOUNT_CURRENCY_MISMATCH.getErrorMessage(),
                        null);
                return ValidationResult.builder().success(false).transferErrorCode(ACCOUNT_CURRENCY_MISMATCH).build();
            }
            
            //START - Existing validation for uae, same applicable for egypt as well
        	CoreCurrencyDto transferCurrency = fetchAllTransferSupportedCurrencies(requestedCurrency, request.getRequestMetaData());
        	if(transferCurrency == null) {
        		log.error("Not able to find requested currency :: [{}] in INFTALL currency list", htmlEscape(requestedCurrency));
        		return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
        	}
        	if(!transferCurrency.isSwiftTransferEnabled()) {
        		log.error("Swift transfer is disabled for this currency [{}]", requestedCurrency);
        		//logFailure(request, metadata);
        		return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
        	}
        	//END - Existing validation for uae, same applicable for egypt as well
            
        	//STORY 91896 INFT - Transaction currency should be same as source account currency
            if(!fromAccount.getCurrency().equals(requestedCurrency)) {
            	log.error("For transation type [{}], transaction currency [{}] should be same as source account currency [{}]",
                        request.getServiceType(), requestedCurrency, fromAccount.getCurrency());
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(),
                        null);
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }

        }
        
        return ValidationResult.builder().success(true).build();
    }
    
    private CoreCurrencyDto fetchAllTransferSupportedCurrencies(String txnCurrency, RequestMetaData metadata) {
    	Response<List<CoreCurrencyDto>> transferCurrencies = mobCommonClient.getTransferCurrencies(function, metadata.getCountry(), txnCurrency);
    	if(transferCurrencies != null && transferCurrencies.hasData() && !transferCurrencies.getData().isEmpty()) {
    		return transferCurrencies.getData().get(0);
    	}
		return null;
	}

    private boolean isWithinRegionTransfer(LocalCurrencyValidationRequest request) {
        return WAMA.getName().equals(request.getServiceType()) || WYMA.getName().equals(request.getServiceType())
                || LOCAL.getName().equals(request.getServiceType());
    }

    @Data
    @Builder
    public static class LocalCurrencyValidationRequest {

        private String serviceType;
        private String transactionCurrency;
        private ValidationContext validationContext;
        private RequestMetaData requestMetaData;
    }
}
