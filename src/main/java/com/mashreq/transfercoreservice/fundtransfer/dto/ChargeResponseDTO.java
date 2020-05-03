package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 5/2/20
 */
@Data
@Builder
public class ChargeResponseDTO {

    private String currency;
    private String chargeAmount;

}
