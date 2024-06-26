package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author shahbazkh
 * @date 3/26/20
 */

@ExtendWith(MockitoExtension.class)
public class BalanceValidatorTest {


    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @InjectMocks
    private BalanceValidator balanceValidator;


    @Test
    public void shouldReturnFailure_whenAvailableBalanceIsLess() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = new AccountDetailsDTO();
        mockAccount.setAvailableBalance(new BigDecimal(1));
        mockAccount.setCurrency("AED");

        mockValidationContext.add("from-account", mockAccount);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setServiceType("local");
        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(10));


        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getTransferErrorCode()).isEqualTo(TransferErrorCode.BALANCE_NOT_SUFFICIENT);
    }

    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsEqual() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = new AccountDetailsDTO();
        mockAccount.setAvailableBalance(new BigDecimal(10));
        mockAccount.setCurrency("AED");

        mockValidationContext.add("from-account", mockAccount);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setServiceType("local");

        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(10));
        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsGreater() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = new AccountDetailsDTO();
        mockAccount.setAvailableBalance(new BigDecimal(10));
        mockAccount.setCurrency("AED");

        mockValidationContext.add("from-account", mockAccount);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(10));
        mockFundTransferRequest.setServiceType("local");

        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(10));
        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }


    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsEqual_andCurrencyDifferent() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = new AccountDetailsDTO();
        		mockAccount.setNumber("12345");
        		mockAccount.setAvailableBalance(new BigDecimal("20"));
        		mockAccount.setCurrency("USD");

        mockValidationContext.add("from-account", mockAccount);
        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(8.17));
        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(20));
        mockFundTransferRequest.setServiceType("local");

        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnFailure_whenAvailableBalanceIsNotSufficient_andCurrencyDifferent() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = new AccountDetailsDTO();
		mockAccount.setNumber("12345");
		mockAccount.setAvailableBalance(new BigDecimal("20"));
		mockAccount.setCurrency("USD");

        mockValidationContext.add("from-account", mockAccount);
        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(108.90));
        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(400));
        mockFundTransferRequest.setServiceType("local");

        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    public void shouldReturnSuccess_whenAvailableBalanceIsSufficient_andCurrencyDifferent() {

        ValidationContext mockValidationContext = new ValidationContext();
        AccountDetailsDTO mockAccount = new AccountDetailsDTO();
		mockAccount.setNumber("12345");
		mockAccount.setAvailableBalance(new BigDecimal("1000"));
		mockAccount.setCurrency("USD");

        mockValidationContext.add("from-account", mockAccount);
        mockValidationContext.add("transfer-amount-in-source-currency", new BigDecimal(108.90));
        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setAmount(new BigDecimal(400));
        mockFundTransferRequest.setServiceType("local");

        ValidationResult result = balanceValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }


}
