package com.mashreq.transfercoreservice.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.mobcommons.cache.MobRedisService;
import com.mashreq.mobcommons.model.DerivedEntitlements;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.mobcommons.utils.ContextCacheKeysSuffix;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mashreq.mobcommons.utils.ContextCacheKeysSuffix.ACCOUNTS;
import static com.mashreq.mobcommons.utils.ContextCacheKeysSuffix.ENTITLEMENTS;
import static com.mashreq.ms.commons.CustomHtmlEscapeUtil.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.USER_SESSION_CONTEXT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionCacheService {
    private static final String ACCOUNT_DETAILS = "-ACCOUNT_DETAILS-MT-";
    public static final String CARD_DETAILS = "-CARD-MT-";
    private static final String ACCOUNT_NUMBERS = "account-numbers";
    public static final String INVESTMENT_ACCOUNT_NUMBERS = "investment-account-number";
    private static final String CARD_NUMBERS = "card-numbers";
    private final MobRedisService redisService;
    private final ObjectMapper objectMapper;
    private static final TypeReference<Map<String, Object>> ACCOUNT_CONTEXT_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};
    private static final TypeReference<Map<String, Object>> CARDS_CONTEXT_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    public boolean isAccountNumberBelongsToCif(final String accountNumber, final String redisKey) {
        Map<String, Object> accountsContext = validateAndReturnContext(redisKey);
        return Optional.ofNullable(accountsContext)
                .map(x -> x.get(ACCOUNT_NUMBERS))
                .map(x -> (List<String>) x)
                .map(x -> x.contains(accountNumber))
                .orElse(false);
    }

	private Map<String, Object> validateAndReturnContext(final String redisKey) {
        final String accountsContextCacheKey = redisKey + ACCOUNTS.getSuffix();
        Map<String, Object> accountsContext = redisService.get(accountsContextCacheKey, ACCOUNT_CONTEXT_TYPE_REFERENCE);
		return accountsContext;
	}

    public boolean isCardNumberBelongsToCif(final String cardNumber, final String redisKey) {
        final String contextCacheKey = redisKey + ContextCacheKeysSuffix.CARDS.getSuffix();
        log.info("Get the redis object for the given key{} ", htmlEscape(contextCacheKey));
        Map<String, Object> contextObj = redisService.get(contextCacheKey, CARDS_CONTEXT_TYPE_REFERENCE);
        if (null == contextObj) {
            GenericExceptionHandler.handleError(USER_SESSION_CONTEXT_NOT_FOUND, USER_SESSION_CONTEXT_NOT_FOUND.getErrorMessage());
        }
        return Optional.ofNullable(contextObj)
                .map(x -> x.get(CARD_NUMBERS))
                .map(x -> (List<String>) x)
                .map(x -> x.contains(cardNumber))
                .orElse(false);


    }

    public String getAccountsDetailsCacheKey(RequestMetaData requestMetaData, String accountNumber) {
        return requestMetaData.getUserCacheKey() + ACCOUNT_DETAILS + accountNumber;
    }

    public String getCardDetailsCacheKey(RequestMetaData requestMetaData, String cardNumber) {
        return requestMetaData.getUserCacheKey() + CARD_DETAILS + cardNumber;
    }
    
    public boolean isMTAccountNumberBelongsToCif(final String accountNumber, final String redisKey) {
    	log.debug("validating whether account belongs to money transfer accounts");
        Map<String, Object> accountsContext = validateAndReturnContext(redisKey);
        boolean isOwnAccount = Optional.ofNullable(accountsContext)
                .map(x -> x.get(ACCOUNT_NUMBERS))
                .map(x -> (List<String>) x)
                .map(x -> x.contains(accountNumber))
                .orElse(false);
        if(!isOwnAccount) {
        	return Optional.ofNullable(accountsContext)
                    .map(x -> x.get(INVESTMENT_ACCOUNT_NUMBERS))
                    .map(x -> (List<String>) x)
                    .map(x -> x.contains(accountNumber))
                    .orElse(false);
        }
        return isOwnAccount;
    }

    public DerivedEntitlements extractEntitlementContext(final String redisKey){
        String entitlementsContextKey = redisKey + ENTITLEMENTS.getSuffix();
        HashMap<String, Object> context = redisService.get(entitlementsContextKey, HashMap.class);
        DerivedEntitlements derivedEntitlements = objectMapper.convertValue(context.get(entitlementsContextKey),
                DerivedEntitlements.class);
        return derivedEntitlements;
    }
}

