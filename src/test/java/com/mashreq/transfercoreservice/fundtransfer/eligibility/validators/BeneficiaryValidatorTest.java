package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryValidatorTest {
    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @InjectMocks
    private BeneficiaryValidator beneficiaryValidator;

    @Test
    public void test_when_beneficiary_is_not_same_as_to_account() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073765");
        requestDTO.setServiceType("test");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertFalse(result.isSuccess());
        assertEquals(TransferErrorCode.BENE_ACC_NOT_MATCH, result.getTransferErrorCode());

    }

    @Test
    public void test_when_beneficiary_is_null() {
        //given
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", null);
        requestDTO.setServiceType("test");
        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertFalse(result.isSuccess());
        assertEquals(TransferErrorCode.BENE_NOT_FOUND, result.getTransferErrorCode());
    }

    @Test
    public void test_when_beneficiary_is_not_active() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.DRAFT.getValue());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("test");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertFalse(result.isSuccess());
        assertEquals(TransferErrorCode.BENE_NOT_ACTIVE_OR_COOLING, result.getTransferErrorCode());
    }

    @Test
    public void test_when_beneficiary_is_active() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("test");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_in_draft_for_fund_transfer() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.DRAFT.name());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("test");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertFalse(result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_active_for_quick_remit() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("quick-remit");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_cooling_for_quick_remit() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.IN_COOLING_PERIOD.name());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("quick-remit");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_draft_for_quick_remit() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.DRAFT.name());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("quick-remit");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertFalse(result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_in_cooling_for_inft() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.IN_COOLING_PERIOD.name());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("INFT");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_when_active_beneficiary_for_inft() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("INFT");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_when_active_beneficiary_for_inft_localcurrency_false() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        beneficiaryDto.setServiceTypeCode("LOCAL");
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("INFT");
        requestDTO.setTxnCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        ReflectionTestUtils.setField(beneficiaryValidator,"localCurrency", "AED");

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        assertFalse(result.isSuccess());
    }


}