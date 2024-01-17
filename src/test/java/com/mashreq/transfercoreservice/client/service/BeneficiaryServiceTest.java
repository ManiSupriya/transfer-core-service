package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryModificationValidationResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.AdditionalFields;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.config.TwoFactorAuthRequiredValidationConfig;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mashreq.transfercoreservice.util.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BeneficiaryServiceTest {

    @Mock
    private BeneficiaryClient beneficiaryClient;
    @Mock
    private AsyncUserEventPublisher userEventPublisher;
    @Mock
    private TwoFactorAuthRequiredValidationConfig config;

    @InjectMocks
    BeneficiaryService beneficiaryService;

    RequestMetaData metaData = RequestMetaData.builder().build();

    @Test
    public void getByIdWithoutValidationV2(){
        when(beneficiaryClient.getByIdWithoutValidationV2(any(), any())).thenReturn(getSuccessResponse(getBeneficiaryDtoV2()));

        BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation("012960001", 1L, "V2", metaData);

        assertNotNull(beneficiaryDto);
        assertEquals(beneficiaryDto.getId().longValue(), 121);
        assertTrue(beneficiaryDto.isNewVersion());
    }

    @Test
    public void getByIdWithoutValidation(){
        when(beneficiaryClient.getByIdWithoutValidation(any(), any())).thenReturn(getSuccessResponse(getBeneficiaryDto()));

        BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation("012960001", 1L, "V1", metaData);

        assertNotNull(beneficiaryDto);
        assertEquals(beneficiaryDto.getId().longValue(), 121);
        assertFalse(beneficiaryDto.isNewVersion());
    }

    @Test
    public void getByIdWithoutValidationError(){
        when(beneficiaryClient.getByIdWithoutValidation(any(), any())).thenReturn(getEmptyErrorResponse());

        Assertions.assertThrows(GenericException.class, () -> beneficiaryService.getByIdWithoutValidation("012960001", 1L, "V1", metaData));

    }

    @Test
    public void getByIdV2(){
        when(beneficiaryClient.getByIdV2(any(), any(), any())).thenReturn(getSuccessResponse(getBeneficiaryDtoV2()));

        BeneficiaryDto beneficiaryDto = beneficiaryService.getById( 1L, "V2", metaData, "international");

        assertNotNull(beneficiaryDto);
        assertEquals(beneficiaryDto.getId().longValue(), 121);
        assertTrue(beneficiaryDto.isNewVersion());
    }

    @Test
    public void getById(){
        when(beneficiaryClient.getById(any(), any(),any())).thenReturn(getSuccessResponse(getBeneficiaryDto()));

        BeneficiaryDto beneficiaryDto = beneficiaryService.getById( 1L, "V1", metaData, "international");

        assertNotNull(beneficiaryDto);
        assertEquals(beneficiaryDto.getId().longValue(), 121);
        assertFalse(beneficiaryDto.isNewVersion());
    }

    @Test
    public void getByIdError(){
        when(beneficiaryClient.getById(any(), any(),any())).thenThrow(GenericException.class);

        Assertions.assertThrows(GenericException.class, () -> beneficiaryService.getById( 1L, "V1", metaData, "international"));

    }

    @Test
    public void updateV2(){
        AdditionalFields additionalFields = new AdditionalFields();

        when(beneficiaryClient.update(any(), any(), any())).thenReturn(getSuccessResponse(getBeneficiaryDtoV2()));

        BeneficiaryDto beneficiaryDto = beneficiaryService.getUpdate( additionalFields, 1L, "V2", metaData, "international");

        assertNotNull(beneficiaryDto);
        assertEquals(beneficiaryDto.getId().longValue(), 121);
        assertTrue(beneficiaryDto.isNewVersion());
    }


    @Test
    public void update(){
        AdditionalFields additionalFields = new AdditionalFields();

        when(beneficiaryClient.update(any(), any(), any())).thenReturn(getSuccessResponse(getBeneficiaryDtoV2()));

        BeneficiaryDto beneficiaryDto = beneficiaryService.getUpdate( additionalFields, 1L, "V2", metaData, "international");

        assertNotNull(beneficiaryDto);
        assertEquals(beneficiaryDto.getId().longValue(), 121);
        assertTrue(beneficiaryDto.isNewVersion());
    }

    @Test
    public void updateError(){
        AdditionalFields additionalFields = new AdditionalFields();

        when(beneficiaryClient.update(any(), any(), any())).thenReturn(getEmptyErrorResponse());

        Assertions.assertThrows(GenericException.class, () -> beneficiaryService.getUpdate( additionalFields, 1L, "V2", metaData, "international"));
    }
    
    @Test
	public void test_isRecentlyUpdated_errorResponse() {
		TwoFactorAuthRequiredCheckRequestDto requestDto = new TwoFactorAuthRequiredCheckRequestDto();
		when(config.getDurationInHours()).thenReturn(24);
		when(beneficiaryClient.isRecentlyUpdated(Mockito.eq(metaData.getPrimaryCif()), Mockito.any()))
				.thenReturn(Response.<BeneficiaryModificationValidationResponse>builder().build());
		assertTrue(beneficiaryService.isRecentlyUpdated(requestDto, metaData, config));
	}
    
    @Test
	public void test_isRecentlyUpdated_emptyData() {
		TwoFactorAuthRequiredCheckRequestDto requestDto = new TwoFactorAuthRequiredCheckRequestDto();
		when(config.getDurationInHours()).thenReturn(24);
		when(beneficiaryClient.isRecentlyUpdated(Mockito.eq(metaData.getPrimaryCif()), Mockito.any()))
				.thenReturn(Response.<BeneficiaryModificationValidationResponse>builder().status(ResponseStatus.SUCCESS).build());
		assertTrue(beneficiaryService.isRecentlyUpdated(requestDto, metaData, config));
	}
    
    @Test
	public void test_isRecentlyUpdated1_withValidResponse() {
		when(config.getDurationInHours()).thenReturn(24);
		BeneficiaryModificationValidationResponse data = new BeneficiaryModificationValidationResponse();
		data.setUpdated(false);
		when(beneficiaryClient.isRecentlyUpdated(Mockito.eq(metaData.getPrimaryCif()), Mockito.any()))
				.thenReturn(Response.<BeneficiaryModificationValidationResponse>builder().status(ResponseStatus.SUCCESS)
						.data(data).build());
		assertEquals(data.isUpdated(), beneficiaryService.isRecentlyUpdated(new TwoFactorAuthRequiredCheckRequestDto(), metaData, config));
	}
}
