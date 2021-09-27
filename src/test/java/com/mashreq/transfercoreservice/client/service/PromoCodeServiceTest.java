package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.promo.service.PromoCodeService;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PromoCodeServiceTest {

    @Mock
    private MobCommonService mobCommonService;

    @Mock
    private BeneficiaryService beneficiaryService;

    @InjectMocks
    private PromoCodeService promoCodeService;

    RequestMetaData metaData = RequestMetaData.builder().build();

    @Test
    public void validateAndSaveEmptyPromo(){
        when(beneficiaryService.getByIdWithoutValidation(any(), any(), any(), any())).thenReturn(TestUtil.getBeneficiaryDto());
        doNothing().when(mobCommonService).validatePromoCode(any());

        Assert.assertFalse(promoCodeService.validateAndSave(new FundTransferRequestDTO(), null, metaData));
    }

    @Test
    public void validateAndSaveValidPromo(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setPromoCode("FREEDOM");
        fundTransferRequestDTO.setBeneficiaryId("1");

        when(beneficiaryService.getByIdWithoutValidation(any(), any(), any(), any())).thenReturn(TestUtil.getBeneficiaryDto());
        doNothing().when(mobCommonService).validatePromoCode(any());

        Assert.assertTrue(promoCodeService.validateAndSave(fundTransferRequestDTO, null, metaData));
    }

    @Test
    public void validateAndSaveInValidPromo(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setPromoCode("FREEDOM");

        when(beneficiaryService.getByIdWithoutValidation(any(), any(), any(), any())).thenReturn(TestUtil.getBeneficiaryDto());
        doThrow(GenericException.class).when(mobCommonService).validatePromoCode(any());

        Assert.assertFalse(promoCodeService.validateAndSave(fundTransferRequestDTO, null, metaData));
    }
}

