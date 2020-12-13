package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BICCodeSearchResponseDto {
    private String bankName;
    private String bankCountry;
    private String bankCity;
    private String swiftCode;
    private String branchCode;
    private String branchName;
}
