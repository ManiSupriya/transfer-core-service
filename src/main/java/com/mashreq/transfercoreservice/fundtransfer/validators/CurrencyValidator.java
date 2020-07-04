package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.event.publisher.AuditEventPublisher;
import com.mashreq.transfercoreservice.event.publisher.Publisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.event.model.EventType.CURRENCY_VALIDATION;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyValidator implements Validator {

    private final Publisher auditEventPublisher;
    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating currency for service type [ {} ] ", request.getServiceType());
        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);
        String requestedCurrency = request.getCurrency();
        log.info("Requested currency [ {} ] service type [ {} ] ", requestedCurrency, request.getServiceType());

        if(WITHIN_MASHREQ.getName().equals(request.getServiceType()) ) {
            BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
            if (beneficiaryDto != null && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), beneficiaryDto.getBeneficiaryCurrency())) {
                log.error("Beneficiary Currency and Requested Currency does not match for service type [ {} ]  ", request.getServiceType());
                auditEventPublisher.publishEvent(CURRENCY_VALIDATION, EventStatus.FAILURE, metadata, null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.CURRENCY_IS_INVALID).build();
            }
        }

        if(isCharityServiceType(request)) {
            CharityBeneficiaryDto charityBeneficiaryDto = context.get("charity-beneficiary-dto", CharityBeneficiaryDto.class);
            if (charityBeneficiaryDto != null && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), charityBeneficiaryDto.getCurrencyCode())) {
                log.error("Charity Currency and Requested Currency does not match for service type [ {} ]  ", request.getServiceType());

                auditEventPublisher.publishEvent(CURRENCY_VALIDATION, EventStatus.FAILURE, metadata, null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.CURRENCY_IS_INVALID).build();
            }
        }

        if(OWN_ACCOUNT.getName().equals(request.getServiceType()) ) {
            AccountDetailsDTO toAccount = context.get("to-account", AccountDetailsDTO.class);
            if (!(requestedCurrency.equals(fromAccount.getCurrency()) || requestedCurrency.equals(toAccount.getCurrency()))) {
                log.error("To Account Currency and Requested Currency does not match for service type [ {} ]  ", request.getServiceType());
                auditEventPublisher.publishEvent(null, EventStatus.FAILURE, metadata, null);
                return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH).build();
            }
        }

        log.info("Currency Validating successful service type [ {} ] ", request.getServiceType());
        auditEventPublisher.publishEvent(null, EventStatus.SUCCESS, metadata, null);
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
