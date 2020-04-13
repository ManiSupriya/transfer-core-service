package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author shahbazkh
 * @date 3/30/20
 */

@RunWith(MockitoJUnitRunner.class)
public class AccountBelongsToCifValidatorTest {

    @Test
    public void shouldReturnFailure_whenBothAccountDoesNotBelongToCif_inOwnAccount() {

        //given
        List<AccountDetailsDTO> listOfMockAccounts = getAccountDetailsDTOS();

        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("account-details", listOfMockAccounts);
        mockValidationContext.add("validate-to-account", true);
        mockValidationContext.add("validate-from-account", true);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setServiceType("own-account");
        mockFundTransferRequest.setToAccount("019010050532");
        mockFundTransferRequest.setFromAccount("019010073000");


        //when
        AccountBelongsToCifValidator accountBelongsToCifValidator = new AccountBelongsToCifValidator();
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getTransferErrorCode()).isEqualTo(TransferErrorCode.ACCOUNT_NOT_BELONG_TO_CIF);
    }

    private List<AccountDetailsDTO> getAccountDetailsDTOS() {
        AccountDetailsDTO fromAcc1 = AccountDetailsDTO.builder()
                .number("010797697124")
                .build();

        AccountDetailsDTO fromAcc2 = AccountDetailsDTO.builder()
                .number("019010050532")
                .build();

        AccountDetailsDTO fromAcc3 = AccountDetailsDTO.builder()
                .number("019010073901")
                .build();

        return Arrays.asList(fromAcc1, fromAcc2, fromAcc3);
    }


    @Test
    public void shouldReturnSuccess_whenBothAccountBelongToCif_inOwnAccount() {

        //given
        List<AccountDetailsDTO> listOfMockAccounts = getAccountDetailsDTOS();

        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("account-details", listOfMockAccounts);
        mockValidationContext.add("validate-to-account", true);
        mockValidationContext.add("validate-from-account", true);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setServiceType("own-account");
        mockFundTransferRequest.setToAccount("019010050532");
        mockFundTransferRequest.setFromAccount("010797697124");


        //when
        AccountBelongsToCifValidator accountBelongsToCifValidator = new AccountBelongsToCifValidator();
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnSuccess_whenToAccountBelongToCif_inMashreq() {

        //given
        List<AccountDetailsDTO> listOfMockAccounts = getAccountDetailsDTOS();

        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("account-details", listOfMockAccounts);
        mockValidationContext.add("validate-to-account", true);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setServiceType("within-mashreq");
        mockFundTransferRequest.setToAccount("019010050532");
        mockFundTransferRequest.setFromAccount("019010073000");


        //when
        AccountBelongsToCifValidator accountBelongsToCifValidator = new AccountBelongsToCifValidator();
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnSuccess_whenToAccountBelongToCif_inLocal() {

        //given
        List<AccountDetailsDTO> listOfMockAccounts = getAccountDetailsDTOS();

        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("account-details", listOfMockAccounts);
        mockValidationContext.add("validate-to-account", true);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setServiceType("local");
        mockFundTransferRequest.setToAccount("019010050532");
        mockFundTransferRequest.setFromAccount("019010073000");


        //when
        AccountBelongsToCifValidator accountBelongsToCifValidator = new AccountBelongsToCifValidator();
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnFailure_whenToAccount_doesnot_belongToCif_inLocal() {

        //given
        AccountDetailsDTO fromAcc1 = AccountDetailsDTO.builder()
                .number("010797697124")
                .build();

        AccountDetailsDTO fromAcc2 = AccountDetailsDTO.builder()
                .number("019010050532")
                .build();

        AccountDetailsDTO fromAcc3 = AccountDetailsDTO.builder()
                .number("019010073901")
                .build();

        List<AccountDetailsDTO> listOfMockAccounts = Arrays.asList(fromAcc1, fromAcc2, fromAcc3);

        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("account-details", listOfMockAccounts);
        mockValidationContext.add("validate-to-account", true);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setServiceType("local");
        mockFundTransferRequest.setToAccount("019010050536");
        mockFundTransferRequest.setFromAccount("019010073000");


        //when
        AccountBelongsToCifValidator accountBelongsToCifValidator = new AccountBelongsToCifValidator();
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }
}
