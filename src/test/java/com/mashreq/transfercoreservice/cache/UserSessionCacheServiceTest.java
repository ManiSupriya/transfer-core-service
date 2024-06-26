package com.mashreq.transfercoreservice.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.mobcommons.cache.MobRedisService;
import com.mashreq.mobcommons.model.DerivedEntitlements;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserSessionCacheServiceTest {

    @Mock
    private MobRedisService redisService;

    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private UserSessionCacheService userSessionCacheService;

    RequestMetaData metaData = RequestMetaData.builder().userCacheKey("1234").build();

    @Test
    public void isAccountNumberBelongsToCif() {
       when(redisService.get(any(), ArgumentMatchers.<TypeReference>any())).thenReturn(TestUtil.getAccountContext());

        assertTrue(userSessionCacheService.isAccountNumberBelongsToCif("0123456789","1234"));
    }

    @Test
    public void test_isMtAccountNumberBelongsToCif_withNormalAccount() {
        when(redisService.get(any(), ArgumentMatchers.<TypeReference>any())).thenReturn(TestUtil.getAccountContext());
        assertTrue(userSessionCacheService.isMTAccountNumberBelongsToCif("0123456789","1234"));
    }
    
    @Test
    public void test_isMtAccountNumberBelongsToCif_withInvestmentAccount() {
        when(redisService.get(any(), ArgumentMatchers.<TypeReference>any())).thenReturn(TestUtil.getMoneyTransferAccountContext());
        assertTrue(userSessionCacheService.isMTAccountNumberBelongsToCif("1123456789","1234"));
    }
    
    @Test
    public void test_isMtAccountNumberBelongsToCif_withUnknownAccount() {
        when(redisService.get(any(), ArgumentMatchers.<TypeReference>any())).thenReturn(TestUtil.getMoneyTransferAccountContext());
        assertFalse(userSessionCacheService.isMTAccountNumberBelongsToCif("1123466789","1234"));
    }
    
    @Test
    public void isCardNumberBelongsToCif() {
        when(redisService.get(any(), ArgumentMatchers.<TypeReference>any())).thenReturn(TestUtil.getCardsContext());

        assertTrue(userSessionCacheService.isCardNumberBelongsToCif("4444333322221111","1234"));
    }

    @Test
    public void getAccountsDetailsCacheKey(){
        assertEquals("1234-ACCOUNT_DETAILS-MT-01234567890",userSessionCacheService.getAccountsDetailsCacheKey(metaData,"01234567890"));
    }

    @Test
    public void getCardDetailsCacheKey(){
        assertEquals("1234-CARD-MT-01234567890",userSessionCacheService.getCardDetailsCacheKey(metaData,"01234567890"));
    }

    @Test
    public void extractEntitlementsContext(){
        Set<String> allowedActions = new HashSet<>();
        allowedActions.add("Inline_MoneyTransfer_Limits_EntryPoint");
        DerivedEntitlements derivedEntitlements = DerivedEntitlements.builder()
                .allowedActions(allowedActions)
                .build();
        HashMap<String, Object> context = new HashMap<>();
        context.put("01234567890-ENTITLEMENTS-CONTEXT",derivedEntitlements);

        when(redisService.get(any(), eq(HashMap.class))).thenReturn(context);
        when(objectMapper.convertValue(any(), eq(DerivedEntitlements.class)))
                .thenReturn(derivedEntitlements);

        DerivedEntitlements actualEntitlements = userSessionCacheService.extractEntitlementContext("01234567890");
        assertTrue(actualEntitlements.getAllowedActions().contains("Inline_MoneyTransfer_Limits_EntryPoint"));
    }
}

