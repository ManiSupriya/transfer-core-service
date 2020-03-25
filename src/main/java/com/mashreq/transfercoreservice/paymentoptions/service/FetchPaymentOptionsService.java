package com.mashreq.transfercoreservice.paymentoptions.service;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentOptionRequest;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentsOptionsResponse;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FunctionalInterface
public interface FetchPaymentOptionsService {

    PaymentsOptionsResponse getPaymentOptions(PaymentOptionRequest request);

    default boolean isPayloadEmpty(List<AccountDetailsDTO> sourceAccounts, List<AccountDetailsDTO> destinationAccounts) {
        return sourceAccounts == null && destinationAccounts == null;
    }
}
