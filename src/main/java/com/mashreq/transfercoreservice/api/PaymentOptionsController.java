package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.paymentoptions.PaymentOptionRequest;
import com.mashreq.transfercoreservice.paymentoptions.PaymentOptionType;
import com.mashreq.transfercoreservice.paymentoptions.PaymentOptionsService;
import com.mashreq.webcore.dto.response.Response;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

import static com.mashreq.transfercoreservice.paymentoptions.PaymentOptionType.getPaymentOptionsByType;

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
                                      @NotNull @PathVariable(required = true) String optionType) {

        log.info("Fetch Payment options for {} ", optionType);
        PaymentOptionType paymentOptionType = getPaymentOptionsByType(optionType);
        PaymentOptionRequest paymentOptionRequest = PaymentOptionRequest.builder()
                .cifId(cifId)
                .paymentOptionType(paymentOptionType)
                .build();

        return Response.builder().data(paymentOptionsService.getPaymentSource(paymentOptionRequest)).build();
    }
}
