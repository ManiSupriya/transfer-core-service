package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.AdditionalFields;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.mashreq.transfercoreservice.util.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryServiceTest {

    @Mock
    private BeneficiaryClient beneficiaryClient;
    @Mock
    private AsyncUserEventPublisher userEventPublisher;

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
}
