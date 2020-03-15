package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.dto.CoreCardDetailsDto;
import com.mashreq.transfercoreservice.config.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FeignClient(name = "cards", url = "${app.services.cards}", configuration = FeignConfig.class)
public interface CardClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/cards/{cifId}/{cardType}")
    Response<List<CoreCardDetailsDto>> getCards(@PathVariable("cifId") String cifId,
                                                @PathVariable("cardType") CardType cardType);

}