package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CURRENCY_IS_INVALID;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyValidator implements Validator<FundTransferRequestDTO> {

    private final AsyncUserEventPublisher auditEventPublisher;
    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating currency for service type [ {} ] ", htmlEscape(request.getServiceType()));
        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);
        String requestedCurrency = request.getCurrency();
        log.info("Requested currency [ {} ] service type [ {} ] ", htmlEscape(requestedCurrency), htmlEscape(request.getServiceType()));

        if(WAMA.getName().equals(request.getServiceType()) ) {
            BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
            SearchAccountDto toAccount = context.get("credit-account-details", SearchAccountDto.class);
            if (beneficiaryDto != null && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), toAccount.getCurrency())) {
                log.error("Beneficiary Currency and Requested Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                        CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null );
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }
        }

        if(isCharityServiceType(request)) {
            CharityBeneficiaryDto charityBeneficiaryDto = context.get("charity-beneficiary-dto", CharityBeneficiaryDto.class);
            if (charityBeneficiaryDto != null && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), charityBeneficiaryDto.getCurrencyCode())) {
                log.error("Charity Currency and Requested Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                        CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }
        }

        if(WYMA.getName().equals(request.getServiceType()) ) {
            AccountDetailsDTO toAccount = context.get("to-account", AccountDetailsDTO.class);
            if (!(requestedCurrency.equals(fromAccount.getCurrency()) || requestedCurrency.equals(toAccount.getCurrency()))) {
                log.error("To Account Currency and Requested Currency does not match for service type [ {} ]  ", htmlEscape(request.getServiceType()));
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), ACCOUNT_CURRENCY_MISMATCH.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH).build();
            }
        }

        log.info("Currency Validating successful service type [ {} ] ", htmlEscape(request.getServiceType()));
        auditEventPublisher.publishSuccessEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }

    private boolean isCharityServiceType(FundTransferRequestDTO request) {
        return BAIT_AL_KHAIR.getName().equals(request.getServiceType())
                || DUBAI_CARE.getName().equals(request.getServiceType())
                || DAR_AL_BER.getName().equals(request.getServiceType());
    }

    private boolean isReqCurrencyValid(String requestedCurrency, String fromAccCurrency, String toCurrency) {
        return requestedCurrency.equals(fromAccCurrency) || requestedCurrency.equals(toCurrency);
    }
}
