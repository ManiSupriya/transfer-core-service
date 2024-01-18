package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.promo.service.PromoCodeService;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromoCodeServiceTest {

    @Mock
    private MobCommonService mobCommonService;

    @Mock
    private BeneficiaryService beneficiaryService;

    @InjectMocks
    private PromoCodeService promoCodeService;

    RequestMetaData metaData = RequestMetaData.builder().build();
    
    @BeforeEach
	public void prepare() {
    	ReflectionTestUtils.setField(promoCodeService, "promocodeDisabled", false);
    }

    @Test
    public void validateAndSaveEmptyPromo(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
    }

    @Test
    public void validateAndSaveValidPromo(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setPromoCode("FREEDOM");
        fundTransferRequestDTO.setBeneficiaryId("1");

        when(beneficiaryService.getByIdWithoutValidation(any(), any(), any(), any())).thenReturn(TestUtil.getBeneficiaryDto());
        doNothing().when(mobCommonService).validatePromoCode(any());

        assertTrue(promoCodeService.validateAndSave(fundTransferRequestDTO, null, metaData));
    }

    @Test
    public void validateAndSaveInValidPromo(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setPromoCode("FREEDOM");
        fundTransferRequestDTO.setBeneficiaryId("1");

        when(beneficiaryService.getByIdWithoutValidation(any(), any(), any(), any())).thenReturn(TestUtil.getBeneficiaryDto());
        doThrow(GenericException.class).when(mobCommonService).validatePromoCode(any());

        assertFalse(promoCodeService.validateAndSave(fundTransferRequestDTO, null, metaData));
    }
    
    @Test
    public void validate_promocodeDisabled(){
    	
    	ReflectionTestUtils.setField(promoCodeService, "promocodeDisabled", true);
    	
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setPromoCode("FREEDOM");
        fundTransferRequestDTO.setBeneficiaryId("1");

       assertFalse(promoCodeService.validateAndSave(fundTransferRequestDTO, null, metaData));
    }
}

