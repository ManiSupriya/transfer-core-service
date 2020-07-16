package com.mashreq.transfercoreservice.client.dto;

import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CoreFundTransferResponseDto {
    private MwResponseStatus mwResponseStatus;
    private String transactionRefNo;
    private String mwResponseCode;
    private String mwReferenceNo;
    private String mwResponseDescription;
    private String externalErrorMessage;
}
