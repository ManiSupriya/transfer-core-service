package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author shahbazkh
 * @date 3/26/20
 */

@RunWith(MockitoJUnitRunner.class)
public class BalanceValidatorTest {

    @InjectMocks
    private BalanceValidator balanceValidator;


    @Test
    public void shouldReturnFailure_whenAvailableBalanceIsLess() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = AccountDetailsDTO.builder()
                .availableBalance(new BigDecimal(1))
                .currency("AED")
                .build();

        mockValidationContext.add("from-account", mockAccount);
        mockValidationContext.add("to-account-currency", "AED");

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setServiceType("local");


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
        mockValidationContext.add("to-account-currency", "AED");

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setServiceType("local");


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
        mockValidationContext.add("to-account-currency", "AED");

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setServiceType("local");


        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }


    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsEqual_andCurrencyDifferent() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = AccountDetailsDTO.builder()
                .number("12345")
                .availableBalance(new BigDecimal("20"))
                .currency("USD")
                .build();

        mockValidationContext.add("from-account", mockAccount);
        mockValidationContext.add("to-account-currency", "AED");
        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(8.17));
        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(20));

        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsNotSufficient_andCurrencyDifferent() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = AccountDetailsDTO.builder()
                .number("12345")
                .availableBalance(new BigDecimal("20"))
                .currency("USD")
                .build();

        mockValidationContext.add("from-account", mockAccount);
        mockValidationContext.add("to-account-currency", "AED");
        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(108.90));
        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(400));

        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }


}
