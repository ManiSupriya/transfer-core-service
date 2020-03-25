package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String cifId;
    private Long userId;
    private Long segmentId;
    private Long countryId;
    private String localCurrency;
}
