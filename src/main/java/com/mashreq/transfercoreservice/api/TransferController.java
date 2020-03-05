package com.mashreq.transfercoreservice.api;

import com.mashreq.webcore.dto.response.Response;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/transfer")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class TransferController {

    @GetMapping
    public Response getPaymentOptions() {
        return Response.builder().data("Hello").build();
    }
}
