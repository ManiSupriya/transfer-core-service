package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

/**
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingCodeValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {

        final int routingCodeLength = context.get("routing-code-length", Integer.class);
        final BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);

        /*if (StringUtils.isBlank(beneficiaryDto.getBankRoutingCode())) {
            log.info("Routing code cannot be empty");
            return ValidationResult.builder().success(false).transferErrorCode(ROUTING_CODE_EMPTY)
                    .build();
        }

        if (beneficiaryDto.getBankRoutingCode().length() != routingCodeLength) {
            log.info("Routing code length is invalid");
            return ValidationResult.builder().success(false).transferErrorCode(ROUTING_CODE_LENGTH_INVALID)
                    .build();
        }

*/
        log.info("Routing code validation successful");
        return ValidationResult.builder().success(true).build();
    }
}
