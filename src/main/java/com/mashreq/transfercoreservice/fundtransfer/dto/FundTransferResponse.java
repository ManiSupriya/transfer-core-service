package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 */
@Data
@Builder(toBuilder = true)
public class FundTransferResponse {

    private CoreFundTransferResponseDto responseDto;
    private String limitVersionUuid;
    private BigDecimal limitUsageAmount;
    private String transactionRefNo;
    private CustomerNotification customerNotification;

}
