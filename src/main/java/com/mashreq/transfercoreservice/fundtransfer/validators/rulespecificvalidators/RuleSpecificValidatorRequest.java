package com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class RuleSpecificValidatorRequest {
    String txnCurrency;
    String sourceAccountCurrency;
    String destinationAccountCurrency;
    BigDecimal txnAmount;
    BeneficiaryDto beneficiary;
    AccountDetailsDTO destinationAccount;
}
