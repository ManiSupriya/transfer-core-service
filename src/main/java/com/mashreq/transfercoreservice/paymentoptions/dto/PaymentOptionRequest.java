package com.mashreq.transfercoreservice.paymentoptions.dto;

import com.mashreq.transfercoreservice.paymentoptions.service.PaymentOptionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Data
@Builder
public class PaymentOptionRequest {
    private String cifId;
    private String channelName;
    private PaymentOptionType paymentOptionType;
    private BigDecimal minAmountToBeAvailable;
}
