package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentOptionRequest;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentsOptionsResponse;
import com.mashreq.transfercoreservice.paymentoptions.service.FinTxnNumberGenerator;
import com.mashreq.transfercoreservice.paymentoptions.service.PaymentOptionType;
import com.mashreq.transfercoreservice.paymentoptions.service.PaymentOptionsService;
import com.mashreq.webcore.dto.response.Response;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

import static com.mashreq.transfercoreservice.paymentoptions.service.PaymentOptionType.getPaymentOptionsByType;

/**
 * @author shahbazkh
 * @date 3/10/20
 */

@Slf4j
@RestController
@RequestMapping("/v1/payment-options")
@Api(value = "Payment Source")
@RequiredArgsConstructor
public class PaymentOptionsController {

    private final PaymentOptionsService paymentOptionsService;

    @GetMapping("/{optionType}")
    public Response getPaymentOptions(@NotNull @RequestHeader("X-CIF-ID") String cifId,
                                      @RequestAttribute("X-CHANNEL-NAME") String channelName,
                                      @PathVariable(required = true) String optionType) {

        log.info("Fetch Payment options for {} ", optionType);
        PaymentOptionType paymentOptionType = getPaymentOptionsByType(optionType);
        PaymentOptionRequest paymentOptionRequest = PaymentOptionRequest.builder()
                .cifId(cifId)
                .channelName(channelName)
                .paymentOptionType(paymentOptionType)
                .build();

        PaymentsOptionsResponse response = paymentOptionsService.getPaymentSource(paymentOptionRequest);
        log.info("Payment Options for option type {} is = {} ", optionType, response);

        return Response.builder().data(response).build();
    }


    //TODO : To be decided if this API is required or not, only use case is try again when it can be needed
    @GetMapping("{optionType}/finTxnNo")
    public Response fixTxNo(@RequestAttribute("X-CHANNEL-TRACE-ID") String channelTraceId,
                            @RequestAttribute("X-CHANNEL-HOST") String channelHost,
                            @RequestAttribute("X-CHANNEL-NAME") String channelName,
                            @RequestHeader("X-CIF-ID") final String cifId,
                            @PathVariable final String optionType) {

        log.info("Request received to generate finTxnNo for {} ", optionType);
        String finTxnNo = FinTxnNumberGenerator.generate(channelName, cifId, getPaymentOptionsByType(optionType));
        log.info("finTxnNo generated for optionType {} ", optionType);
        return Response.builder().data(finTxnNo).build();
    }
}
