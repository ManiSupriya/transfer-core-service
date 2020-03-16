package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 3/15/20
 */
@Data
@Builder(toBuilder = true)
public class FundTransferMetadata {
    private String primaryCif;
    private String channel;
    private String channelHost;
    private String channelTraceId;
}
