package com.mashreq.transfercoreservice.cache;
import static com.mashreq.mobcommons.utils.ContextCacheKeysSuffix.ACCOUNTS;
/**
 * Suresh Pasupuleti
 */
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.USER_SESSION_CONTEXT_NOT_FOUND;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mashreq.ms.commons.cache.IAMSessionUser;
import com.mashreq.ms.exceptions.GenericExceptionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionCacheService {
	private static final String ACCOUNT_NUMBERS = "account-numbers";
    private final MobRedisService redisService;
    private static final TypeReference<Map<String, Object>> ACCOUNT_CONTEXT_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};
    
    public boolean isAccountNumberBelongsToCif(final String accountNumber, final String redisKey) {
        IAMSessionUser iamSessionUser = redisService.get(redisKey, IAMSessionUser.class);
        assertUserSessionContextPresent(iamSessionUser);
        final String accountsContextCacheKey = redisKey + ACCOUNTS.getSuffix();
        Map<String, Object> accountsContext = redisService.get(accountsContextCacheKey, ACCOUNT_CONTEXT_TYPE_REFERENCE);

        return Optional.ofNullable(accountsContext)
                .map(x -> x.get(ACCOUNT_NUMBERS))
                .map(x -> (List<String>) x)
                .map(x -> x.contains(accountNumber))
                .orElse(false);
    }
    
    public void assertUserSessionContextPresent(IAMSessionUser iamSessionUser) {
        if (null == iamSessionUser)
            GenericExceptionHandler.handleError(USER_SESSION_CONTEXT_NOT_FOUND, USER_SESSION_CONTEXT_NOT_FOUND.getErrorMessage());
    }
}

