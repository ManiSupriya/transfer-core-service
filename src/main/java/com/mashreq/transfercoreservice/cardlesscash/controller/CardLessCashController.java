package com.mashreq.transfercoreservice.cardlesscash.controller;

import java.math.BigInteger;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.transfercoreservice.cardlesscash.constants.CardLessCashConstants;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.webcore.dto.response.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
@RequestMapping(CardLessCashConstants.URL.CARD_LESS_CASH_BASE_URL)
@RestController
@AllArgsConstructor
@Api(tags = {"Account MicroService"})
public class CardLessCashController {

    private CardLessCashService cardLessCashService;

    /**
     * Block the generated CLC(Card Less Cash) request.
     *
     * @param blockRequest CardLessCashBlockRequest
     * @return Response<CardLessCashBlockResponse>
     */
    @ApiOperation("This operation is responsible for blocking/cancelling generated card less cash request.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Response for card less cash block request."),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource")
    })
    @PostMapping(CardLessCashConstants.URL.CLC_BLOCK_URL)
    public Response<CardLessCashBlockResponse> blockCardLessCashRequest(@RequestBody CardLessCashBlockRequest blockRequest) {

        return cardLessCashService.blockCardLessCashRequest(blockRequest);
    }
    
    /**
     * the CLC(Card Less Cash) generation request.
     *
     * @param Request CardLessCashGenerationRequest
     * @return Response<CardLessCashGenerationResponse>
     */
    @ApiOperation("This operation is responsible for remit generation for card less cash request.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Response for card less cash generation request."),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource")
    })
    @PostMapping(CardLessCashConstants.URL.CLC_REQUEST_URL)
    public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(@RequestBody CardLessCashGenerationRequest cardLessCashGenerationRequest) {

    	return cardLessCashService.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest);
    }
    
    /**
     * the CLC(Card Less Cash) generation request.
     *
     * @param Request CardLessCashGenerationRequest
     * @return Response<CardLessCashGenerationResponse>
     */
    @ApiOperation("This operation is responsible for remit generation for card less cash request.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Response for card less cash generation request."),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource")
    })
    @GetMapping(CardLessCashConstants.URL.CLC_QUERY_URL)
    public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(@PathVariable final String accountNumber, @RequestParam final Integer remitNumDays) {
    	CardLessCashQueryRequest cardLessCashQueryRequest = new CardLessCashQueryRequest();
    	cardLessCashQueryRequest.setAccountNumber(accountNumber);
    	cardLessCashQueryRequest.setRemitNumDays(remitNumDays);
    	return cardLessCashService.cardLessCashRemitQuery(cardLessCashQueryRequest);
    }

}
