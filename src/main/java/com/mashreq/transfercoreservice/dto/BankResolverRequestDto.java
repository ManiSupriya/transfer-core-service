package com.mashreq.transfercoreservice.dto;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankResolverRequestDto {

    private String journeyType;
    private RequestMetaData requestMetaData;
    private String identifier;
    private String bankCode;
    private String branchCode;

}
