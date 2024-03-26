package com.mashreq.transfercoreservice.cardlesscash.controller;


import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.cardlesscash.advice.CardlessCashGenerationTwoFaAdvice;
import com.mashreq.transfercoreservice.cardlesscash.constants.CardLessCashConstants;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequestV2;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.webcore.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mashreq.twofa.annotations.RequiredTwoFa;
import org.springframework.web.bind.annotation.*;

import static com.mashreq.ms.commons.cache.HeaderNames.X_USSM_USER_LOGIN_ID;
import static com.mashreq.ms.commons.cache.HeaderNames.X_USSM_USER_MOBILE_NUMBER;
import static com.mashreq.transfercoreservice.common.CommonConstants.CARD_LESS_CASH;
import static com.mashreq.transfercoreservice.common.CommonConstants.X_REQUEST_METADATA;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@RequestMapping(CardLessCashConstants.URL.CARD_LESS_CASH_BASE_URL_V2)
@RestController
@AllArgsConstructor
@Tag(name = "CardLess Cash Controller")
@Slf4j
public class CardLessCashControllerV2 {

    private CardLessCashService cardLessCashService;
	private AccountService accountService;
    private AsyncUserEventPublisher asyncUserEventPublisher;
    private UserSessionCacheService userSessionCacheService;


    /**
     * the CLC(Card Less Cash) generation request.
     *
     * @param //Request CardLessCashGenerationRequestV2
     * @return Response<CardLessCashGenerationResponse>
     */
    @Operation( summary="This operation is responsible for remit generation for card less cash request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response for card less cash generation request."),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource")
    })
    @PostMapping(CardLessCashConstants.URL.CLC_REQUEST_URL)
	@RequiredTwoFa(twoFaAdviceClass = CardlessCashGenerationTwoFaAdvice.class)
	public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(
			@RequestHeader(X_USSM_USER_LOGIN_ID) final String userId,
			@RequestHeader(X_USSM_USER_MOBILE_NUMBER) final String userMobileNumber,
			@RequestAttribute(X_REQUEST_METADATA) RequestMetaData metaData,
			@Valid @RequestBody CardLessCashGenerationRequestV2 cardLessCashGenerationRequestV2) {
		log.info("cardLessCash GenerationRequest {} ", htmlEscape(cardLessCashGenerationRequestV2));
		asyncUserEventPublisher.publishStartedEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_REQUEST, metaData,
				CARD_LESS_CASH);
		assertMobileNo(userMobileNumber, metaData);
		checkAccountBelongsToUser(cardLessCashGenerationRequestV2.getAccountNo(), metaData);

		var cardlessCashRequest = CardLessCashGenerationRequest.builder()
				.accountNo(cardLessCashGenerationRequestV2.getAccountNo())
				.amount(cardLessCashGenerationRequestV2.getAmount())
				.build();
		var cardLessCashGenerationResponse = cardLessCashService
				.cardLessCashRemitGenerationRequest(cardlessCashRequest, userMobileNumber, userId, metaData);
		log.info("cardLessCash generate Response {} ", htmlEscape(cardLessCashGenerationResponse));
		asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_REQUEST, metaData,
				CARD_LESS_CASH);
		return cardLessCashGenerationResponse;
	}
    
	private void checkAccountBelongsToUser(final String accountNumber, RequestMetaData metaData){
		accountService.getAccountsIfNotInCache(metaData);
		assertAccountBelongsToUser(accountNumber,metaData);
	}
    private void assertAccountBelongsToUser(final String accountNumber, RequestMetaData metaData) {
 	   log.info("cardLessCash  Account Details {} Validation with User", htmlEscape(accountNumber));
         if (!userSessionCacheService.isAccountNumberBelongsToCif(accountNumber, metaData.getUserCacheKey())) {
         	asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_ACCOUNT_NUMBER_DOES_NOT_MATCH,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
         			TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.toString(),
					TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage(),
         			TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage());
             GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF,
					 TransferErrorCode.ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage());
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
         	asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_MOBILE_NUMBER_DOES_NOT_MATCH,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
					TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.toString(),
					TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.getErrorMessage(),
       			TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.getErrorMessage());
             GenericExceptionHandler.handleError(TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH,
					 TransferErrorCode.MOBILE_NUMBER_DOES_NOT_MATCH.getErrorMessage());
         }
     }

}
