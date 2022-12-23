package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransferLimitResponseDto {
    private boolean success;
    private String errorCode;
    private String errorMessage;
}

