package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.webcore.dto.response.Response;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class for the CardLessCashService
 */
@Slf4j
@Service
@AllArgsConstructor
public class CardLessCashServiceImpl implements CardLessCashService {
	private final AccountService accountService;

    @Override
    public Response blockCardLessCashRequest(CardLessCashBlockRequest blockRequest) {
    	return accountService.blockCardLessCashRequest(blockRequest);
}

	@Override
	public Response cardLessCashRemitGenerationRequest(
			CardLessCashGenerationRequest cardLessCashGenerationRequest) {
		return accountService.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest);
}
	@Override
	public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(CardLessCashQueryRequest cardLessCashQueryRequest) {
		return accountService.cardLessCashRemitQuery(cardLessCashQueryRequest.getAccountNumber(), cardLessCashQueryRequest.getRemitNumDays());
}

}
