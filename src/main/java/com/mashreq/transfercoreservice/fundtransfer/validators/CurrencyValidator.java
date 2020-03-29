package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.fundtransfer.ServiceType.*;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
public class CurrencyValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {

        log.info("Validating currency for service type [ {} ] ", request.getServiceType());
        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);
        String requestedCurrency = request.getCurrency();
        log.info("Requested currency [ {} ] service type [ {} ] ", requestedCurrency, request.getServiceType());

        BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
        if (WITHIN_MASHREQ.getName().equals(request.getServiceType()) && beneficiaryDto != null && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), beneficiaryDto.getBeneficiaryCurrency())) {
            log.warn("Beneficiary Currency and Requested Currency does not match for service type [ {} ]  ", request.getServiceType());
            return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.CURRENCY_IS_INVALID).build();
        }

        CharityBeneficiaryDto charityBeneficiaryDto = context.get("charity-beneficiary-dto", CharityBeneficiaryDto.class);
        if (CHARITY_ACCOUNT.getName().equals(request.getServiceType()) && charityBeneficiaryDto != null && !isReqCurrencyValid(requestedCurrency, fromAccount.getCurrency(), charityBeneficiaryDto.getAccountNumber())) {
            log.warn("Charity Currency and Requested Currency does not match for service type [ {} ]  ", request.getServiceType());
            return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.CURRENCY_IS_INVALID).build();
        }

        AccountDetailsDTO toAccount = context.get("to-account", AccountDetailsDTO.class);
        if (OWN_ACCOUNT.getName().equals(request.getServiceType()) && !(requestedCurrency.equals(fromAccount.getCurrency()) || requestedCurrency.equals(toAccount.getCurrency()))) {
            log.warn("To Account Currency and Requested Currency does not match for service type [ {} ]  ", request.getServiceType());
            return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH).build();
        }

        log.info("Currency Validating successful service type [ {} ] ", request.getServiceType());
        return ValidationResult.builder().success(true).build();

    }

    private boolean isReqCurrencyValid(String requestedCurrency, String fromAccCurrency, String toCurrency) {
        return requestedCurrency.equals(fromAccCurrency) || requestedCurrency.equals(toCurrency);
    }
}
