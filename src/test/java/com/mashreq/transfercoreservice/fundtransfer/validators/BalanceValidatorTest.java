package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author shahbazkh
 * @date 3/26/20
 */

@RunWith(MockitoJUnitRunner.class)
public class BalanceValidatorTest {

    @Test
    public void shouldReturnFailure_whenAvailableBalanceIsLess() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = AccountDetailsDTO.builder()
                .availableBalance(new BigDecimal(1))
                .currency("AED")
                .build();

        mockValidationContext.add("from-account", mockAccount);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setCurrency("AED");
        mockFundTransferRequest.setServiceType("local");

        BalanceValidator balanceValidator = new BalanceValidator(null);
        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getTransferErrorCode()).isEqualTo(TransferErrorCode.BALANCE_NOT_SUFFICIENT);
    }

    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsEqual() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = AccountDetailsDTO.builder()
                .availableBalance(new BigDecimal(10))
                .currency("AED")
                .build();

        mockValidationContext.add("from-account", mockAccount);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setCurrency("AED");
        mockFundTransferRequest.setServiceType("local");

        BalanceValidator balanceValidator = new BalanceValidator(null);
        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsGreater() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = AccountDetailsDTO.builder()
                .availableBalance(new BigDecimal(20))
                .currency("AED")
                .build();

        mockValidationContext.add("from-account", mockAccount);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setCurrency("AED");
        mockFundTransferRequest.setServiceType("local");

        BalanceValidator balanceValidator = new BalanceValidator(null);
        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }


    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsEqual_andCurrencyDifferent() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = AccountDetailsDTO.builder()
                .availableBalance(new BigDecimal("7.34"))
                .currency("USD")
                .build();

        mockValidationContext.add("from-account", mockAccount);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(20));
        mockFundTransferRequest.setCurrency("AED");
        mockFundTransferRequest.setServiceType("local");

        BalanceValidator balanceValidator = new BalanceValidator(null);
        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
}
