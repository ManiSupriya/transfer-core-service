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

}
