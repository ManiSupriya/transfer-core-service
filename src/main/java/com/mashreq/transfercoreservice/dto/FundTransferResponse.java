package com.mashreq.transfercoreservice.dto;

import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
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
