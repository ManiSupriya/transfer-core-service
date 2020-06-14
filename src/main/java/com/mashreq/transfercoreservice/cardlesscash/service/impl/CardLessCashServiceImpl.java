package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.esbcore.validator.EsbResponseValidator;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class for the CardLessCashService
 */
@Slf4j
@Service
@AllArgsConstructor
public class CardLessCashServiceImpl implements CardLessCashService {


    @Override
    public Response blockCardLessCashRequest(CardLessCashBlockRequest blockRequest) {
		return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(CardLessCashBlockResponse.builder()
                        .build())
                .build();
}

	@Override
	public Response cardLessCashRemitGenerationRequest(
			CardLessCashGenerationRequest cardLessCashGenerationRequest) {
		return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(CardLessCashGenerationResponse.builder()
                        .build())
                .build();
}
	@Override
	public Response cardLessCashRemitQuery(CardLessCashQueryRequest cardLessCashQueryRequest) {
		return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(CardLessCashQueryResponse.builder()
                        .build())
                .build();
}

}
