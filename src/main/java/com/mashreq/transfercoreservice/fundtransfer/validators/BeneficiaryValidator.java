package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus.ACTIVE;
import static com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus.IN_COOLING_PERIOD;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

/**
 * @author shahbazkh
 * @date 3/18/20
 */
@Slf4j
@Component
public class BeneficiaryValidator implements Validator {

    private static final String QUICK_REMIT = "quick-remit";


    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        final BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
        log.info("Validating Beneficiary for service type [ {} ] ", request.getServiceType());

        if (beneficiaryDto == null)
            return ValidationResult.builder().success(false).transferErrorCode(BENE_NOT_FOUND)
                    .build();

        if (!beneficiaryDto.getAccountNumber().equals(request.getToAccount()))
            return ValidationResult.builder().success(false).transferErrorCode(BENE_ACC_NOT_MATCH)
                    .build();

        if (QUICK_REMIT.equals(request.getServiceType())) {
            return validateBeneficiaryStatus(Arrays.asList(ACTIVE.name(), IN_COOLING_PERIOD.name()),
                    beneficiaryDto.getStatus(), BENE_NOT_ACTIVE_OR_COOLING);
        }

        log.info("Beneficiary validation successful for service type [ {} ] ", request.getServiceType());
        return validateBeneficiaryStatus(Arrays.asList(ACTIVE.name()), beneficiaryDto.getStatus(), BENE_NOT_ACTIVE);
    }

    private ValidationResult validateBeneficiaryStatus(List<String> validStatus, String beneStatus, TransferErrorCode errorCode) {

        if (validStatus.contains(beneStatus)) {
            return ValidationResult.builder().success(true).build();
        } else {
            return ValidationResult.builder().success(false).transferErrorCode(errorCode)
                    .build();
        }
    }


}
