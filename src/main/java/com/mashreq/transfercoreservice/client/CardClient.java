package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.dto.CoreCardDetailsDto;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FeignClient(name = "cards", url = "${app.services.cards}", configuration = FeignConfig.class)
public interface CardClient {

    @GetMapping( value = "/api/cards/{cifId}/{cardType}")
    Response<List<CoreCardDetailsDto>> getCards(@PathVariable("cifId") String cifId,
                                                @PathVariable("cardType") CardType cardType);

}
