package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.cache.MobRedisService;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.CardClient;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardClient cardClient;
    @Mock
    private UserSessionCacheService userSessionCacheService;
    @Mock
    private MobRedisService mobRedisService;

    @InjectMocks
    CardService cardService;

    @Test
    public void getCardDetailsFromCache(){
        when(userSessionCacheService.isCardNumberBelongsToCif(any(), any())).thenReturn(true);
        when(cardClient.getCardDetails(any())).thenReturn(TestUtil.getSuccessResponse(TestUtil.getCardDetails()));

        CardDetailsDTO cardDetailsDTO = cardService.getCardDetailsFromCache("123456789012", RequestMetaData.builder().build());

        assertNotNull(cardDetailsDTO);
        assertEquals("2020-08-04",cardDetailsDTO.getExpiryDate());
    }

    @Test
    public void getCardDetailsFromCacheError(){
        when(userSessionCacheService.isCardNumberBelongsToCif(any(), any())).thenReturn(false);

        Assertions.assertThrows(GenericException.class, () -> {
            cardService.getCardDetailsFromCache("123456789012", RequestMetaData.builder().build());
        });
    }

    @Test
    public void getCardDetailsServiceError(){
        when(userSessionCacheService.isCardNumberBelongsToCif(any(), any())).thenReturn(true);
        when(cardClient.getCardDetails(any())).thenReturn(TestUtil.getErrorResponse(TestUtil.getCardDetails()));

        Assertions.assertThrows(GenericException.class, () -> {
            cardService.getCardDetailsFromCache("123456789012", RequestMetaData.builder().build());
        });
    }

}
