package com.mashreq.transfercoreservice.fundtransfer.validators;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DESTINATION_ACCOUNT_FREEZED_FOR_CREDIT;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.SOURCE_ACCOUNT_FREEZED_FOR_DEBIT;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_FREEZE_VALIDATION;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_IS_UNDER_CREDIT_FREEZE;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_IS_UNDER_DEBIT_FREEZE;

import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author indrajith
 * @date 3/21/21
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountFreezeValidator implements Validator<FundTransferRequestDTO> {

    private final AsyncUserEventPublisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating account freeze for service type [ {} ] ", htmlEscape(request.getServiceType()));

        final Boolean validateCreditFreeze = context.get("validate-credit-freeze", Boolean.class);
        final Boolean validateDebitFreeze = context.get("validate-debit-freeze", Boolean.class);
        /** to fetch account Details */
        final SearchAccountDto creditAccount = context.get("credit-account-details", SearchAccountDto.class);
        final SearchAccountDto debitAccount = context.get("debit-account-details", SearchAccountDto.class);

        if(validateCreditFreeze != null && validateCreditFreeze && creditAccount != null && isAccountCreditFreezed(creditAccount)){
            auditEventPublisher.publishFailureEvent(ACCOUNT_IS_UNDER_CREDIT_FREEZE, metadata, null,
            		DESTINATION_ACCOUNT_FREEZED_FOR_CREDIT.getErrorMessage(), DESTINATION_ACCOUNT_FREEZED_FOR_CREDIT.getCustomErrorCode(), null);
            return prepareValidationResult(Boolean.FALSE,DESTINATION_ACCOUNT_FREEZED_FOR_CREDIT);
        }
        
        if(validateDebitFreeze != null && validateDebitFreeze && debitAccount != null  && isAccountDebitFreezed(debitAccount)){
            auditEventPublisher.publishFailureEvent(ACCOUNT_IS_UNDER_DEBIT_FREEZE, metadata, null,
            		SOURCE_ACCOUNT_FREEZED_FOR_DEBIT.getErrorMessage(), SOURCE_ACCOUNT_FREEZED_FOR_DEBIT.getCustomErrorCode(), null);
            return prepareValidationResult(Boolean.FALSE,SOURCE_ACCOUNT_FREEZED_FOR_DEBIT);
        }
        
        log.info("Freeze validation Successful for service type [ {} ] ", htmlEscape(request.getServiceType()));
        auditEventPublisher.publishSuccessEvent(ACCOUNT_FREEZE_VALIDATION, metadata, null);
        return prepareValidationResult(Boolean.TRUE);
    }

    private boolean isAccountDebitFreezed(SearchAccountDto debitAccount) {
    	if(debitAccount != null) {
    		return debitAccount.isNoDebit() || debitAccount.isNoDebitForCompliance() ;
    	}
    	return false;
	}

	private boolean isAccountCreditFreezed(SearchAccountDto creditAccount) {
		if(creditAccount != null ) {
			return creditAccount.isNoCredit();
		}
		return false;
	}

	private ValidationResult prepareValidationResult(boolean status,TransferErrorCode errCode) {
        return ValidationResult.builder()
                .success(status)
                .transferErrorCode(errCode)
                .build();
    }
	
	private ValidationResult prepareValidationResult(boolean status) {
        return ValidationResult.builder()
                .success(status)
                .build();
    }

}
