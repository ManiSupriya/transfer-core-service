package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/22/20
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CountryDto {

    private String code;
    private String name;
    private Boolean active;
    private String riskGroupType;
    private String nativeCurrency;
    private Iban iban;
    private RoutingCode routingCode;
    private Boolean addressLine1Required;
    private Integer addressMinLength;
    private Integer addressMaxLength;
    private String addressRegex;
    private List<AdditionalField> mandatoryAttributes;
    private List<AdditionalField> optionalAttributes;
    private FormattingRules formattingRules;
    private boolean isQuickRemitEnabled;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FormattingRules {
        private String beneficiaryNameRegex;
        private Integer beneficiaryNameMinLength;
        private Integer beneficiaryNameMaxLength;
        private boolean beneficiarySingleNameAllowed;
        private String beneficiaryCityRegex;
        private Integer beneficiaryCityLength;
        private String beneficiaryStateRegex;
        private Integer beneficiaryStateLength;
        private String beneficiaryPostalCodeRegex;
        private Integer beneficiaryPostalCodeLength;
        private String beneficiaryMobileNumberRegex;
        private Integer beneficiaryMobileNumberMinLength;
        private Integer beneficiaryMobileNumberMaxLength;
        private String accountNumberRegex;
        private Integer accountNumberMinLength;
        private Integer accountNumberMaxLength;
    }

    /**
     * @author shahbazkh
     * @date 3/22/20
     */

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Iban {
        private Integer length;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RoutingCode {
        private String code;
        private Integer length;
        private String routingCodeRegex;
    }
}
