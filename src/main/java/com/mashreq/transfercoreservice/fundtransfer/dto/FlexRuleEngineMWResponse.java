package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Bean;

/**
 * @author shahbazkh
 * @date 5/2/20
 */
@Data
@Builder
public class FlexRuleEngineMWResponse {

    private String productCode;
    private String chargeCurrency;
    private String chargeAmount;
}
