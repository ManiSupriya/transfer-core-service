package com.mashreq.transfercoreservice.paymentoptions.service;

import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentOptionRequest;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentsOptionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.mashreq.transfercoreservice.paymentoptions.service.PaymentOptionType.*;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Slf4j
@Service
@TrackExecTimeAndResult
@RequiredArgsConstructor
public class PaymentOptionsService {

    private Map<PaymentOptionType, FetchPaymentOptionsService> paymentOptionsLookUp;
    private final TransferOptionsOwnAccount transferOptionsOwnAccount;
    private final TransferOptionsDefault transferOptionsDefault;

    @PostConstruct
    public void init() {
        paymentOptionsLookUp = new HashMap<>();
        paymentOptionsLookUp.put(TRANSFER_OPTION_OWN_ACC, transferOptionsOwnAccount);
        paymentOptionsLookUp.put(TRANSFER_OPTION_MASHREQ, transferOptionsDefault);
        paymentOptionsLookUp.put(TRANSFER_OPTION_LOCAL, transferOptionsDefault);
        paymentOptionsLookUp.put(TRANSFER_OPTION_INTERNATIONAL, transferOptionsDefault);
    }

    public PaymentsOptionsResponse getPaymentSource(PaymentOptionRequest request) {
        PaymentOptionType paymentOptionType = request.getPaymentOptionType();
        log.info("Fetching payments options for {} ", paymentOptionType);

        FetchPaymentOptionsService fetchPaymentOptionsService = paymentOptionsLookUp.get(paymentOptionType);
        PaymentsOptionsResponse response = fetchPaymentOptionsService.getPaymentOptions(request);
        log.info("Fetched Payment Options {} ", response);

        return response;
    }
}
