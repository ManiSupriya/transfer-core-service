package com.mashreq.transfercoreservice.fundtransfer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mashreq.mobcommons.cache.MobRedisService;
import com.mashreq.ms.exceptions.GenericException;

import com.mashreq.transfercoreservice.client.dto.CountryDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static java.time.Instant.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MobCommonServiceTest {

    @Mock
    private MobCommonClient mobCommonClient;
    @Mock
    private MobRedisService mobRedisService;

    @InjectMocks
    private MobCommonService mobCommonService;

    @Test
    public void testCountryValidationRulesFromCache() {
        when(mobRedisService.get(any(), ArgumentMatchers.<TypeReference<Map<String, CountryDto>>>any())).thenReturn(TestUtil.getCountryMap());

        CountryDto countryDto = mobCommonService.getCountryValidationRules("IN");
        Assertions.assertEquals("IN", countryDto.getCode());
    }

    @Test
    public void testCountryValidationRulesFromAPISuccess() {
        when(mobRedisService.get(any(), ArgumentMatchers.<TypeReference<Map<String, CountryDto>>>any())).thenReturn(Collections.emptyMap());
        when(mobCommonClient.getCountryValidationRule(any())).thenReturn(TestUtil.getSuccessResponse(TestUtil.getCountryMs()));
        CountryDto countryDto = mobCommonService.getCountryValidationRules("IN");
        Assertions.assertEquals("IN", countryDto.getCode());
    }

    @Test
    public void testCountryValidationRulesFromAPIError() {
        when(mobRedisService.get(any(), ArgumentMatchers.<TypeReference<Map<String, CountryDto>>>any())).thenReturn(Collections.emptyMap());
        when(mobCommonClient.getCountryValidationRule(any())).thenReturn(TestUtil.getErrorResponse(TestUtil.getCountryMs()));

        Assertions.assertThrows(GenericException.class, ()->{
            mobCommonService.getCountryValidationRules("IN");
        });
    }
}
