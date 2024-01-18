package com.mashreq.transfercoreservice.api;

import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS;
import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS_GENERATE;
import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS_URL;
import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS_VALIDATE;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.transfercoreservice.loyaltysmilecard.service.IccLoyaltySmileCardService;
import com.mashreq.webcore.dto.response.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(SMILE_REWARDS_URL)
@Tag(name = SMILE_REWARDS)
@RequiredArgsConstructor
public class IccLoyaltySmileCardController {
	private final IccLoyaltySmileCardService iccLoyaltySmileCardService;
	
	@Operation(summary = "This operation is responsible for generate session ID for loyalty rewards.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfully processed"),
			@ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
			@ApiResponse(responseCode = "500", description = "Something went wrong") })
	@GetMapping(SMILE_REWARDS_GENERATE)
	public Response<Object> generateRedeemID(@RequestHeader(HeaderNames.CIF_HEADER_NAME) final String cifId,
            @RequestHeader(HeaderNames.X_USSM_USER_REDIS_KEY) final String userCacheKey) {
		log.info("generting loyalty redeem ID for cif {} ", htmlEscape(cifId));
		return iccLoyaltySmileCardService.generateRedeemIDforSmileCard(cifId, userCacheKey);

	}
	
	@Operation(summary = "This operation is responsible for validate session ID for loyalty rewards.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfully processed"),
			@ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
			@ApiResponse(responseCode = "500", description = "Something went wrong") })
	@GetMapping(SMILE_REWARDS_VALIDATE)
	public Response<Object> validateRedeemID(@RequestHeader(HeaderNames.CIF_HEADER_NAME) final String cifId,
			@RequestParam(required = true) String sessionID) {
		log.info("validating loyalty redeem ID {} ", htmlEscape(sessionID));
		return iccLoyaltySmileCardService.validateRedeemIDforSmileCard(cifId, sessionID);

	}
}
