package com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;

public interface TwoFactorAuthRequiredCheckService {

	public TwoFactorAuthRequiredCheckResponseDto checkIfTwoFactorAuthenticationRequired(RequestMetaData metaData,
			TwoFactorAuthRequiredCheckRequestDto request);

}
