package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.PostConstruct;
import java.util.EnumMap;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class QuickRemitStrategyTest {

    @InjectMocks
    private QuickRemitStrategy quickRemitStrategy;

    @Mock
    private QuickRemitIndiaStrategy quickRemitIndiaStrategy;

    @Mock
    private QuickRemitPakistanStrategy quickRemitPakistanStrategy;

    @Mock
    private BeneficiaryService beneficiaryService;

    @Mock
    EnumMap<QuickRemitType, QuickRemitFundTransfer> fundTransferStrategies;



    @Test
    public void test_quick_remit_india() {
        //Given

        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setBeneficiaryId("123");
        FundTransferMetadata metadata = FundTransferMetadata.builder().primaryCif("12345").build();
        UserDTO dto = new UserDTO();
        BeneficiaryDto bene = new BeneficiaryDto();
        bene.setBeneficiaryCountryISO("IN");
        ValidationContext validateContext = new ValidationContext();
        validateContext.add("beneficiary-dto", bene);

        //when
        quickRemitStrategy.init();
        Mockito.when(beneficiaryService.getById(Mockito.eq("12345"), Mockito.eq(123L))).thenReturn(bene);

        quickRemitStrategy.execute(requestDTO, metadata, dto);
        //then
        Mockito.verify(quickRemitIndiaStrategy, Mockito.atLeastOnce()).execute(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

    }

    @Test
    public void test_quick_remit_pakistan() {
        //Given

        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setBeneficiaryId("123");
        FundTransferMetadata metadata = FundTransferMetadata.builder().primaryCif("12345").build();
        UserDTO dto = new UserDTO();
        BeneficiaryDto bene = new BeneficiaryDto();
        bene.setBeneficiaryCountryISO("PK");
        ValidationContext validateContext = new ValidationContext();
        validateContext.add("beneficiary-dto", bene);

        //when
        quickRemitStrategy.init();
        Mockito.when(beneficiaryService.getById(Mockito.eq("12345"), Mockito.eq(123L))).thenReturn(bene);

        quickRemitStrategy.execute(requestDTO, metadata, dto);
        //then
        Mockito.verify(quickRemitPakistanStrategy, Mockito.atLeastOnce()).execute(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

    }


}
