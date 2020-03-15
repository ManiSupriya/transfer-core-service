package com.mashreq.transfercoreservice.client.dto;

import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoreFundTransferResponseDto {
    private MwResponseStatus mwResponseStatus;
    private String transactionRefNo;
    private String mwResponseCode;
    private String mwReferenceNo;
    private String mwResponseDescription;
}