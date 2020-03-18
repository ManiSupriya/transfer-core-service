package com.mashreq.transfercoreservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.transfercoreservice.errors.FundTransferErrorMapper;
import com.mashreq.transfercoreservice.errors.FundTransferException;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Slf4j
public class FeignCustomErrorDecoder implements ErrorDecoder {

    public static final String ERROR_CODE = "errorCode";
    public static final String MESSAGE = "message";

    @Override
    public Exception decode(String s, Response response) {


        try {
            log.error("Error occurred in Feign {} ", response.request().url());
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            JsonNode jsonNode = mapper.readTree(response.body().asReader());

            log.error("Error Response from Feign {} {} {} ",
                    response.request().url(), jsonNode.get(ERROR_CODE).asText(),
                    jsonNode.get(MESSAGE).asText());
            TransferErrorCode errorCode = FundTransferErrorMapper.getTransferErrorCode(jsonNode.get(ERROR_CODE).asText());
            return new FundTransferException(errorCode, jsonNode.get(MESSAGE).asText());

        } catch (IOException e) {
            // Adding this catch block but ideally this should never
            // as it is only for asReader() call
            log.error("Error while reading Feign Error Response {} ", e);
            return new RuntimeException(e);
        }
    }

}
