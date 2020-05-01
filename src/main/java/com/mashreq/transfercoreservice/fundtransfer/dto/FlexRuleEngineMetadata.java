package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
@Builder
public class FlexRuleEngineMetadata {
    private String cifId;
    private String channelTraceId;
}
