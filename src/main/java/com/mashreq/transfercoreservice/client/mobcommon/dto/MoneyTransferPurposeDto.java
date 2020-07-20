package com.mashreq.transfercoreservice.client.mobcommon.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class MoneyTransferPurposeDto {

    private String purposeCode;
    private String purposeDesc;

}
