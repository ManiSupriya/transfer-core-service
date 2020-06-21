package com.mashreq.transfercoreservice.cardlesscash.dto.request;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is to hold the detail for blocking the CLC request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardLessCashQueryRequest {

    private String accountNumber;
    private BigInteger remitNumDays;
}
