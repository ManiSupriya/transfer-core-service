package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 5/2/20
 */
@Data
@Builder
public class ChargeResponseDTO {

    private String chargeCurrency;
    private BigDecimal chargeAmount;
    private BigDecimal debitAmount;
    private BigDecimal totalDebitAmount;

}
