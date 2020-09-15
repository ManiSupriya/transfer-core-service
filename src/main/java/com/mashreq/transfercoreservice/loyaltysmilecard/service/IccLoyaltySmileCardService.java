package com.mashreq.transfercoreservice.loyaltysmilecard.service;

import com.mashreq.webcore.dto.response.Response;

public interface IccLoyaltySmileCardService {

	Response<Object> generateRedeemIDforSmileCard(String cifId, String userCacheKey);
	Response<Object> validateRedeemIDforSmileCard(String cifId, String sessionID);
}
