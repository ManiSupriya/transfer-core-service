package com.mashreq.transfercoreservice.client;

import java.math.BigInteger;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenReq;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.client.dto.CifProductsDto;
import com.mashreq.transfercoreservice.client.dto.CoreAccountDetailsDTO;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.webcore.dto.response.Response;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FeignClient(name = "accounts", url = "${app.services.accounts}", configuration = FeignConfig.class)
public interface AccountClient {

    @GetMapping(value = "/api/accounts/cif/{cifId}")
    Response<CifProductsDto> searchAccounts(@PathVariable("cifId") String cifId, @RequestParam(required = false) List<String> linkedCifs);


    @RequestMapping(method = RequestMethod.GET, value = "/api/accounts/{accountNumber}")
    Response<CoreAccountDetailsDTO> getAccountDetails(@PathVariable("accountNumber") String accountNumber);
    
    @GetMapping(value = "/api/cardless-cash/query/{accountNumber}")
    public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(@PathVariable("accountNumber") final String accountNumber, @RequestParam  Integer remitNumDays);
    
    @PostMapping(value = "/api/cardless-cash/request-block")
    public Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest);
    
    @PostMapping(value = "/api/cardless-cash/request")
    public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(CardLessCashGenReq cardLessCashGenReq);
}
