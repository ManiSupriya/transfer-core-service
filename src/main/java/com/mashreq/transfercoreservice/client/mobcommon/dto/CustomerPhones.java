package com.mashreq.transfercoreservice.client.mobcommon.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerPhones {

    private String phoneNumberType;
    private String phoneNumber;
    private String mobNumber;
}
