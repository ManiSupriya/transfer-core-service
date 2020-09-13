package com.mashreq.transfercoreservice.loyalty.service;

import com.mashreq.webcore.dto.response.Response;

public interface IccLoyaltyService {

	Response generateRedeemID(String cifId, String userCacheKey);
	Response validateRedeemID(String sessionID);
}
