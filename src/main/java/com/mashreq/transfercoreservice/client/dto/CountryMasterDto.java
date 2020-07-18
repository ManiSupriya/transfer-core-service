package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shahbazkh
 * @date 3/24/20
 */

@Data
@NoArgsConstructor
public class CountryMasterDto {
    private String code;
    private String name;
    private Boolean active;
    private String phoneCode;
    private RiskGroupType riskGroupType;
    private Boolean ibanRequired;
    private Integer ibanLength;
    private String routingCode;
    private Integer routingCodeLength;
    private String nativeCurrency;
    private Boolean quickRemitEnabled;
    private Boolean addressRequired;
    private String region;
    private String channel;
}
