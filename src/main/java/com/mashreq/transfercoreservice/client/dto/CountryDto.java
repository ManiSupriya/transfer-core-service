package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class CountryDto {
    private Integer id;
    private String code;
    private String name;
    private boolean active;
    private String phoneCode;
    private RiskGroupType riskGroupType;
    @JsonIgnore
    private TransferRuleDto ruleDto;

    public Boolean isIbanRequired() {
        return Optional.ofNullable(ruleDto).map(TransferRuleDto::isIbanRequired).orElse(null);
    }

    public Boolean isRoutingCodeRequired() {
        return Optional.ofNullable(ruleDto).map(TransferRuleDto::isRoutingCodeRequired).orElse(null);
    }

    public Integer getMaxLength() {
        return Optional.ofNullable(ruleDto).map(TransferRuleDto::getMaxLength).orElse(null);
    }

    public String getRoutingTypeCode() {
        return Optional.ofNullable(ruleDto).map(TransferRuleDto::getRoutingTypeCode).orElse(null);
    }

    public String getRoutingDescription() {
        return Optional.ofNullable(ruleDto).map(TransferRuleDto::getRoutingCodeName).orElse(null);
    }

    public Boolean isAddressRequired() {
        return Optional.ofNullable(ruleDto).map(TransferRuleDto::isAddressRequired).orElse(null);
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransferRuleDto {
        private boolean routingCodeRequired;
        private boolean ibanRequired;
        private String routingTypeCode;
        private String routingCodeName;
        private Integer maxLength;
        private boolean isAddressRequired;

        public TransferRuleDto(TransferRule rule) {
            this.ibanRequired = rule.isIbanRequired();
            this.routingCodeRequired = rule.isRoutingCodeRequired();
            this.routingCodeName = rule.getRoutingCodeName();
            this.routingTypeCode = rule.getRoutingTypeCode();
            this.maxLength = rule.getMaxLength();
            this.isAddressRequired = rule.isAddressRequired();
        }
    }

}