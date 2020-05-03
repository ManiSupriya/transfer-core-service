package com.mashreq.transfercoreservice.client.mobcommon.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
public class MoneyTransferPurposeDto {

    private String purposeCode;
    private String purposeDesc;

}
