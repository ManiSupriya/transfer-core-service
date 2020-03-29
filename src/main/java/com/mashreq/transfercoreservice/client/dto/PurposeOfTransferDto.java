package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class PurposeOfTransferDto {

    private String country;
    private String popCode;
    private String popDesc;
    private String internalPop;
    private String popGroup;
    private String transactionType;
    private String customerType;
    private String mappingRequired;
    private String popLanguage;
    private String enabled;
}
