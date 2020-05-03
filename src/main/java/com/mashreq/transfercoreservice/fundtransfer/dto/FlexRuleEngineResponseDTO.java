package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
@Builder
public class FlexRuleEngineResponseDTO {
    private String productCode;
}
