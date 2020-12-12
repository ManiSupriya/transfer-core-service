package com.mashreq.transfercoreservice.cardlesscash.controller;

import static com.mashreq.ms.commons.cache.HeaderNames.*;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.cardlesscash.constants.CardLessCashConstants;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;

import static com.mashreq.transfercoreservice.common.CommonConstants.*;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import com.mashreq.webcore.dto.response.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RequestMapping(CardLessCashConstants.URL.CARD_LESS_CASH_BASE_URL)
@RestController
@AllArgsConstructor
@Api(tags = {"TransferCore MicroService"})
@Slf4j
public class CardLessCashController {

    private CardLessCashService cardLessCashService;
    private AsyncUserEventPublisher asyncUserEventPublisher;
    private UserSessionCacheService userSessionCacheService;

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
    public Response<CardLessCashBlockResponse> blockCardLessCashRequest(@RequestAttribute(X_REQUEST_METADATA) RequestMetaData metaData, @Valid @RequestBody CardLessCashBlockRequest blockRequest) {
    	log.info("cardLessCash  blockRequest {} ", htmlEscape(blockRequest));
    	asyncUserEventPublisher.publishStartedEvent(FundTransferEventType.CARD_LESS_CASH_BLOCK_REQUEST, metaData, CARD_LESS_CASH);
    	assertAccountBelongsToUser(blockRequest.getAccountNumber(), metaData);
    	Response<CardLessCashBlockResponse> cardLessCashBlockResponse = cardLessCashService.blockCardLessCashRequest(blockRequest, metaData);
    	log.info("cardLessCash  blockResponse {} ", htmlEscape(cardLessCashBlockResponse));
    	asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.CARD_LESS_CASH_BLOCK_REQUEST, metaData, CARD_LESS_CASH);
    	return cardLessCashBlockResponse;
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
	public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(
			@RequestHeader(X_USSM_USER_LOGIN_ID) final String userId,
			@RequestHeader(X_USSM_USER_MOBILE_NUMBER) final String userMobileNumber,
			@RequestAttribute(X_REQUEST_METADATA) RequestMetaData metaData,
			@Valid @RequestBody CardLessCashGenerationRequest cardLessCashGenerationRequest) {
		log.info("cardLessCash GenerationRequest {} ", htmlEscape(cardLessCashGenerationRequest));
		asyncUserEventPublisher.publishStartedEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_REQUEST, metaData,
				CARD_LESS_CASH);
		assertMobileNo(userMobileNumber, metaData);
		assertAccountBelongsToUser(cardLessCashGenerationRequest.getAccountNo(), metaData);
		Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = cardLessCashService
				.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, userMobileNumber, userId, metaData);
		log.info("cardLessCash generate Response {} ", htmlEscape(cardLessCashGenerationResponse));
		asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_REQUEST, metaData,
				CARD_LESS_CASH);
		return cardLessCashGenerationResponse;
	}
    
    /**
     * the CLC(Card Less Cash) generation request.
     *
     * @param Request CardLessCashGenerationRequest
     * @return Response<CardLessCashGenerationResponse>
     */
    @ApiOperation("This operation is responsible for query details for card less cash.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Response for card less cash query details."),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource")
    })
	@GetMapping(CardLessCashConstants.URL.CLC_QUERY_URL)
	public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(
			@RequestAttribute(X_REQUEST_METADATA) RequestMetaData metaData, @PathVariable final String accountNumber,
			@RequestParam final Integer remitNumDays) {
		CardLessCashQueryRequest cardLessCashQueryRequest = new CardLessCashQueryRequest();
		cardLessCashQueryRequest.setAccountNumber(accountNumber);
		cardLessCashQueryRequest.setRemitNumDays(remitNumDays);
		log.info("cardLessCash  Query Details {} ", htmlEscape(cardLessCashQueryRequest));
		asyncUserEventPublisher.publishStartedEvent(FundTransferEventType.CARD_LESS_CASH_QUERY_DETAILS, metaData,
				CARD_LESS_CASH);
		assertAccountBelongsToUser(accountNumber, metaData);
		Response<List<CardLessCashQueryResponse>> cardLessCashQueryResponse = cardLessCashService
				.cardLessCashRemitQuery(cardLessCashQueryRequest, metaData);
		log.info("cardLessCash  Query Result {} ", htmlEscape(cardLessCashQueryResponse));
		asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.CARD_LESS_CASH_QUERY_DETAILS, metaData,
				CARD_LESS_CASH);
		return cardLessCashQueryResponse;
	}
    
    private void assertAccountBelongsToUser(final String accountNumber, RequestMetaData metaData) {
 	   log.info("cardLessCash  Account Details {} Validation with User", htmlEscape(accountNumber));
         if (!userSessionCacheService.isAccountNumberBelongsToCif(accountNumber, metaData.getUserCacheKey())) {
         	asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_ACCOUNT_NUMBER_DOES_NOT_MATCH, metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
         			TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.toString(), TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage(),
         			TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage());
             GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF, TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage());
         }
     }
     
     private void assertMobileNo(final String mobileNumber, RequestMetaData metaData) {
     	log.info("cardLessCash  mobileNumber {} Validation", htmlEscape(mobileNumber));
     	 /*
         * Bug 35838 - BE|Cardless cash -Getting request failed due to validation of mobile digit number
         * 
         *  change: 
         *  As discussed with NavaKumar the mobile no can contain 12 digits or 10 digits, so we are checking only whether mobile no is empty or not
         * 
         */
     	if (mobileNumber.isEmpty()) {
         	asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_MOBILE_NUMBER_DOES_NOT_MATCH, metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
         			TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.toString(), TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.getErrorMessage(),
       			TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.getErrorMessage());
             GenericExceptionHandler.handleError(TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH, TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.getErrorMessage());
         }
     }

}
