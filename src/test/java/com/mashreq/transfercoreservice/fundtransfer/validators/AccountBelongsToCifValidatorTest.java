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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author shahbazkh
 * @date 3/30/20
 */

@ExtendWith(MockitoExtension.class)
public class AccountBelongsToCifValidatorTest {



    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @InjectMocks
    private AccountBelongsToCifValidator accountBelongsToCifValidator;


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
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getTransferErrorCode()).isEqualTo(TransferErrorCode.ACCOUNT_NOT_BELONG_TO_CIF);
    }

    private List<AccountDetailsDTO> getAccountDetailsDTOS() {
        AccountDetailsDTO fromAcc1 = new AccountDetailsDTO();
        fromAcc1.setNumber("010797697124");
        
        AccountDetailsDTO fromAcc2 = new AccountDetailsDTO();
        fromAcc2.setNumber("019010050532");
        
        AccountDetailsDTO fromAcc3 = new AccountDetailsDTO();
        fromAcc3.setNumber("019010073901");

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
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnFailure_whenToAccount_doesnot_belongToCif_inLocal() {

        //given
    	AccountDetailsDTO fromAcc1 = new AccountDetailsDTO();
        fromAcc1.setNumber("010797697124");
        
        AccountDetailsDTO fromAcc2 = new AccountDetailsDTO();
        fromAcc2.setNumber("019010050532");
        
        AccountDetailsDTO fromAcc3 = new AccountDetailsDTO();
        fromAcc3.setNumber("019010073901");

        List<AccountDetailsDTO> listOfMockAccounts = Arrays.asList(fromAcc1, fromAcc2, fromAcc3);

        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("account-details", listOfMockAccounts);
        mockValidationContext.add("validate-to-account", true);

        FundTransferRequestDTO mockFundTransferRequest = new FundTransferRequestDTO();
        mockFundTransferRequest.setServiceType("local");
        mockFundTransferRequest.setToAccount("019010050536");
        mockFundTransferRequest.setFromAccount("019010073000");


        //when
        ValidationResult result = accountBelongsToCifValidator.validate(mockFundTransferRequest, null, mockValidationContext);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }
}
