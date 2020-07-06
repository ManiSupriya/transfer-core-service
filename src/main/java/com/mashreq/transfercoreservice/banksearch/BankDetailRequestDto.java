package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.transfercoreservice.annotations.ConditionalRequired;
import com.mashreq.transfercoreservice.annotations.ValueOfEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Data
@ConditionalRequired(fieldName = "countryCode", dependentFieldName = "type", noneMatch = {"iban"}, message = "Country is required")
@ConditionalRequired(fieldName = "routingCodeType", dependentFieldName = "type", anyMatch = {"routing-code"}, message = "Routing code type is required")
public class BankDetailRequestDto {

    @NotEmpty
    @ValueOfEnum(enumClass = BankCodeType.class, message = "Not a valid bank code type")
    private String type;

    private String routingCodeType;

    @NotEmpty
    private String value;

    private String countryCode;


}
