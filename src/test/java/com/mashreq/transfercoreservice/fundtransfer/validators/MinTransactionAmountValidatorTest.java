package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import com.mashreq.transfercoreservice.model.ServiceType;
import com.mashreq.transfercoreservice.repository.ServiceTypeRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class MinTransactionAmountValidatorTest {

    @InjectMocks
    private MinTransactionAmountValidator minTransactionAmountValidator;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @Test
    public void test_min_validator_executed_only_if_configured() {
        //Given
        FundTransferRequestDTO request = new FundTransferRequestDTO();
        request.setServiceType("WAMA");
        RequestMetaData metadata = new RequestMetaData();
        metadata.setPrimaryCif("cif1234");
        ValidationContext context = new ValidationContext();
        context.add("transfer-amount-for-min-validation", new BigDecimal("1"));

        ServiceType serviceType = new ServiceType();
        serviceType.setCode("WAMA");
        serviceType.setMinAmount("5");
        //When
        Mockito.when(serviceTypeRepository.findByCodeEquals("WAMA")).thenReturn(Optional.of(serviceType));
        ValidationResult result = minTransactionAmountValidator.validate(request, metadata, context);


        //Then
        Assert.assertFalse(result.isSuccess());

    }

    @Test
    public void test_min_validator_not_executed_if_not_configured() {
        //Given
        FundTransferRequestDTO request = new FundTransferRequestDTO();
        request.setServiceType("WAMA");
        RequestMetaData metadata = new RequestMetaData();
        metadata.setPrimaryCif("cif1234");
        ValidationContext context = new ValidationContext();
        context.add("transfer-amount-for-min-validation", new BigDecimal("1"));

        ServiceType serviceType = new ServiceType();
        serviceType.setCode("WAMA");
        //When
        Mockito.when(serviceTypeRepository.findByCodeEquals("WAMA")).thenReturn(Optional.of(serviceType));
        ValidationResult result = minTransactionAmountValidator.validate(request, metadata, context);


        //Then
        Assert.assertTrue(result.isSuccess());

    }

    @Test
    public void test_min_validator_executed_and_was_successful() {
        //Given
        FundTransferRequestDTO request = new FundTransferRequestDTO();
        request.setServiceType("WAMA");
        RequestMetaData metadata = new RequestMetaData();
        metadata.setPrimaryCif("cif1234");
        ValidationContext context = new ValidationContext();
        context.add("transfer-amount-for-min-validation", new BigDecimal("10"));

        ServiceType serviceType = new ServiceType();
        serviceType.setCode("WAMA");
        serviceType.setMinAmount("5");

        //When
        Mockito.when(serviceTypeRepository.findByCodeEquals("WAMA")).thenReturn(Optional.of(serviceType));
        ValidationResult result = minTransactionAmountValidator.validate(request, metadata, context);


        //Then
        Assert.assertTrue(result.isSuccess());

    }

    @Test
    public void test_min_validator_executed_and_failed() {
        //Given
        FundTransferRequestDTO request = new FundTransferRequestDTO();
        request.setServiceType("WAMA");
        RequestMetaData metadata = new RequestMetaData();
        metadata.setPrimaryCif("cif1234");
        ValidationContext context = new ValidationContext();
        context.add("transfer-amount-for-min-validation", new BigDecimal("4"));

        ServiceType serviceType = new ServiceType();
        serviceType.setCode("WAMA");
        serviceType.setMinAmount("5");

        //When
        Mockito.when(serviceTypeRepository.findByCodeEquals("WAMA")).thenReturn(Optional.of(serviceType));
        ValidationResult result = minTransactionAmountValidator.validate(request, metadata, context);


        //Then
        Assert.assertFalse(result.isSuccess());

    }
}
