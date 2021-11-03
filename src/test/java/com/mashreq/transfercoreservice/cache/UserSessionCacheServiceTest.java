package com.mashreq.transfercoreservice.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.mobcommons.utils.ContextCacheKeysSuffix;
import com.mashreq.ms.commons.cache.IAMSessionUser;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.util.TestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mashreq.mobcommons.utils.ContextCacheKeysSuffix.ACCOUNTS;
import static com.mashreq.ms.commons.CustomHtmlEscapeUtil.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.USER_SESSION_CONTEXT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserSessionCacheServiceTest {

    @Mock
    private MobRedisService redisService;
    @InjectMocks
    private UserSessionCacheService userSessionCacheService;

    RequestMetaData metaData = RequestMetaData.builder().userCacheKey("1234").build();

    @Test
    public void isAccountNumberBelongsToCif() {
        when(redisService.get(any(), ArgumentMatchers.<Class>any())).thenReturn(new IAMSessionUser());
        when(redisService.get(any(), ArgumentMatchers.<TypeReference>any())).thenReturn(TestUtil.getAccountContext());

        assertTrue(userSessionCacheService.isAccountNumberBelongsToCif("0123456789","1234"));
    }

    @Test
    public void isCardNumberBelongsToCif() {
        when(redisService.get(any(), ArgumentMatchers.<Class>any())).thenReturn(new IAMSessionUser());
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
}

