package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 3/15/20
 */
@Data
@Builder(toBuilder = true)
public class RequestMetaData {
    private String primaryCif;
    private String channel;
    private String channelHost;
    private String channelTraceId;
    private String coRelationId;
    private String userCacheKey;
    private String username;
    private String actionKey;
    private String ip;
    private String region;
}
