package com.mashreq.transfercoreservice.soap.transfer;

import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MashreqFundTransferMWResponse {
    private MwResponseStatus mwResponseStatus;
    private String billRefNo;
    private String mwResponseCode;
    private String mwReferenceNo;
    private String mwResponseDescription;
}
