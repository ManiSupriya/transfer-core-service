package com.mashreq.transfercoreservice.client;

import com.mashreq.mobcommons.services.http.RequestMetaData;

import java.util.HashMap;
import java.util.Map;

import static com.mashreq.mobcommons.constants.Constants.X_MOB_LANGUAGE;
import static com.mashreq.ms.commons.cache.HeaderNames.*;

/**
 * Created by KrishnaKo on 15/12/2023
 */
public class RequestMetadataMapper {


    
    public static Map<String,String> collectRequestMetadataAsMap(RequestMetaData requestMetaData){
         Map<String,String> headerMap = new HashMap<>();
        headerMap.put(CIF_HEADER_NAME, requestMetaData.getPrimaryCif());
        headerMap.put(X_USSM_EMAIL_ID,requestMetaData.getEmail());
        headerMap.put(CHANNEL_TYPE_HEADER_NAME, requestMetaData.getChannel());
        headerMap.put(X_USSM_USER_MOBILE_NUMBER,requestMetaData.getMobileNUmber());
        headerMap.put(COUNTRY_HEADER_NAME, requestMetaData.getCountry());
        headerMap.put(X_USSM_USER_NAME, requestMetaData.getUsername());   
        headerMap.put(X_USSM_USER_IAM_ID, requestMetaData.getDigitalUserId());
        headerMap.put(X_USSM_USER_LOGIN_ID, requestMetaData.getLoginId());
        headerMap.put(X_USSM_USER_REGION, requestMetaData.getRegion());
        headerMap.put(X_USSM_USER_DEVICE_IP, requestMetaData.getDeviceIP());
        headerMap.put(X_CORRELATION_ID, requestMetaData.getCoRelationId());
        headerMap.put(X_USSM_USER_REDIS_KEY, requestMetaData.getUserCacheKey());
        headerMap.put(X_MOB_LANGUAGE, requestMetaData.getLanguage());
        headerMap.put("ORIGIN", "FEIGN");
        return headerMap;
    }
}
