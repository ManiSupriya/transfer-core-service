package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.config.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumMap;

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
        RequestMetaData metadata = RequestMetaData.builder().primaryCif("12345").build();
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
        RequestMetaData metadata = RequestMetaData.builder().primaryCif("12345").build();
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
