package com.mashreq.transfercoreservice.paymentoptions;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FunctionalInterface
public interface FetchPaymentOptionsService {

    PaymentsOptionsResponse getPaymentOptions(PaymentOptionRequest request);

    default BigDecimal getMinAmountToBeAvailable(PaymentOptionRequest request) {
        return request.getMinAmountToBeAvailable() == null ? BigDecimal.ZERO : request.getMinAmountToBeAvailable();
    }
}
