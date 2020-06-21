package com.mashreq.transfercoreservice.cardlesscash.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

/**
 * This class is to hold the detail for blocking the CLC response.
 */
@Data
@Builder
public class CardLessCashQueryResponse {

    private String status;
    private BigDecimal amount;
    private String remitNo;
    private LocalDate transactionDate;
    private String channelName;
    private LocalDate redeemedDate;
}
