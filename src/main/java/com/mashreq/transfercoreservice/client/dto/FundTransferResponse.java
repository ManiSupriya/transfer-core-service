package com.mashreq.transfercoreservice.client.dto;

import lombok.Builder;
import lombok.Data;

/**
 *
 */
@Data
@Builder(toBuilder = true)
public class FundTransferResponse {

    private CoreFundTransferResponseDto responseDto;
    private String limitVersionUuid;

}
