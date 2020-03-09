package com.mashreq.transfercoreservice.paymentoptions;

import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.mashreq.transfercoreservice.paymentoptions.PaymentOptionType.*;

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
    private final FetchBillPaymentSourcesService fetchBillPaymentSources;
    private final FetchFundTransferSourcesService fetchFundTransferSources;
    private final FetchOwnFundTransferDestinationService fetchOwnFundTransferDestination;

    @PostConstruct
    public void init() {
        paymentOptionsLookUp = new HashMap<>();
        paymentOptionsLookUp.put(BILL_PAYMENTS_SOURCE, fetchBillPaymentSources);
        paymentOptionsLookUp.put(FUND_TRANSFER_SOURCE, fetchFundTransferSources);
        paymentOptionsLookUp.put(FUND_TRANSFER_OWN_ACCOUNT_DESTINATION, fetchOwnFundTransferDestination);
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
