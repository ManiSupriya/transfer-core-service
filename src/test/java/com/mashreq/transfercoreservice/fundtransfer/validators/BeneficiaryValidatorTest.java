package com.mashreq.transfercoreservice.fundtransfer.validators;


import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.event.publisher.Publisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Supplier;


@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryValidatorTest {


    @Mock
    private Publisher auditEventPublisher;

    @InjectMocks
    private BeneficiaryValidator beneficiaryValidator;

    @Test
    public void test_when_beneficiary_is_not_same_as_to_account() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073765");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(false, result.isSuccess());
        Assert.assertEquals(TransferErrorCode.BENE_ACC_NOT_MATCH, result.getTransferErrorCode());

    }

    @Test
    public void test_when_beneficiary_is_null() {
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", null);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(false, result.isSuccess());
        Assert.assertEquals(TransferErrorCode.BENE_NOT_FOUND, result.getTransferErrorCode());
    }

    @Test
    public void test_when_beneficiary_is_not_active() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.DRAFT.getValue());
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(false, result.isSuccess());
        Assert.assertEquals(TransferErrorCode.BENE_NOT_ACTIVE, result.getTransferErrorCode());
    }

    @Test
    public void test_when_beneficiary_is_active() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_in_draft_for_fund_transfer() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.DRAFT.name());
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(false, result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_active_for_quick_remit() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("quick-remit");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_cooling_for_quick_remit() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.IN_COOLING_PERIOD.name());
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("quick-remit");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_when_beneficiary_is_draft_for_quick_remit() {
        //given
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019010073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.DRAFT.name());
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("quick-remit");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = beneficiaryValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(false, result.isSuccess());
    }
}
