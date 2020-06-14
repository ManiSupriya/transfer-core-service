package com.mashreq.transfercoreservice.cardlesscash.service;

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.webcore.dto.response.Response;

/**
 * Interface for holding CLC operations.
 */
public interface CardLessCashService {

    Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest);
    Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(CardLessCashGenerationRequest cardLessCashGenerationRequest);
    Response<CardLessCashQueryResponse> cardLessCashRemitQuery(CardLessCashQueryRequest cardLessCashQueryRequest);
}
