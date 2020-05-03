package com.mashreq.transfercoreservice.client.mobcommon.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressTypeDto {
    private String addressType;
    private String prefFlag;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String address5;
}