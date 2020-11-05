package com.mashreq.transfercoreservice.api;

import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS;
import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS_GENERATE;
import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS_URL;
import static com.mashreq.transfercoreservice.loyaltysmilecard.common.CommonConstants.SMILE_REWARDS_VALIDATE;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.transfercoreservice.loyaltysmilecard.service.IccLoyaltySmileCardService;
import com.mashreq.webcore.dto.response.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(SMILE_REWARDS_URL)
@Api(value = SMILE_REWARDS)
@RequiredArgsConstructor
public class IccLoyaltySmileCardController {
	private final IccLoyaltySmileCardService iccLoyaltySmileCardService;
	
	@ApiOperation("This operation is responsible for generate session ID for loyalty rewards.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "successfully processed"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 500, message = "Something went wrong") })
	@GetMapping(SMILE_REWARDS_GENERATE)
	public Response<Object> generateRedeemID(@RequestHeader(HeaderNames.CIF_HEADER_NAME) final String cifId,
            @RequestHeader(HeaderNames.X_USSM_USER_REDIS_KEY) final String userCacheKey) {
		log.info("generting loyalty redeem ID for cif {} ", htmlEscape(cifId));
		return iccLoyaltySmileCardService.generateRedeemIDforSmileCard(cifId, userCacheKey);

	}
	
	@ApiOperation("This operation is responsible for validate session ID for loyalty rewards.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "successfully processed"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 500, message = "Something went wrong") })
	@GetMapping(SMILE_REWARDS_VALIDATE)
	public Response<Object> validateRedeemID(@RequestHeader(HeaderNames.CIF_HEADER_NAME) final String cifId,
			@RequestParam(required = true) String sessionID) {
		log.info("validating loyalty redeem ID {} ", htmlEscape(sessionID));
		return iccLoyaltySmileCardService.validateRedeemIDforSmileCard(cifId, sessionID);

	}
}
