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

@Slf4j
@RestController
@RequestMapping("/v1/transfer")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class FundTransferController {

    private final PaymentOptionsService paymentOptionsService;

    @GetMapping("/options/{filterType}")
    public Response getPaymentOptions(@NotNull @RequestHeader("X-CIF-ID") String cifId,
                                      @NotNull @PathVariable String filterType) {

        log.info("Fetch Payment options for {} ", filterType);
        PaymentOptionType paymentOptionType = getPaymentOptionsByType(filterType);
        PaymentOptionRequest paymentOptionRequest = PaymentOptionRequest.builder()
                .cifId(cifId)
                .paymentOptionType(paymentOptionType)
                .build();

        return Response.builder().data(paymentOptionsService.getPaymentSource(paymentOptionRequest)).build();
    }
}
