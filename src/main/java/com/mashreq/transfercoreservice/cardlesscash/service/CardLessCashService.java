package com.mashreq.transfercoreservice.cardlesscash.service;

import java.util.List;

import com.mashreq.mobcommons.services.http.RequestMetaData;
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

    Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest, RequestMetaData metaData);
    Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(CardLessCashGenerationRequest cardLessCashGenerationRequest, String userMobileNumber, String userId, RequestMetaData metaData);
    Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(CardLessCashQueryRequest cardLessCashQueryRequest, RequestMetaData metaData);
}
