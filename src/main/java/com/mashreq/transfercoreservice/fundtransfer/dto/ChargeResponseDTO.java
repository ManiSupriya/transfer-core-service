package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 5/2/20
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargeResponseDTO {

    private String chargeCurrency;
    private BigDecimal chargeAmount;
    private BigDecimal debitAmount;
    private BigDecimal totalDebitAmount;

}
