package com.mashreq.transfercoreservice.client.mobcommon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDetailsDto {

    private List<AddressTypeDto> address;
    private String nationality;
    private String cifBranch;
    private String cifId;
    private String cifStatus;
    private String customerFullName;
    private String uniqueIDName;
    private String uniqueIDValue;
    private List<CustomerPhones> phones;



}
