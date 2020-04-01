package com.mashreq.transfercoreservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EXTERNAL_SERVICE_ERROR;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Slf4j
public class FeignCustomErrorDecoder implements ErrorDecoder {

    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_ID = "errorId";
    public static final String ERROR_DETAIL = "errorDetails";
    public static final String MESSAGE = "message";
    public static final String EMPTY_STRING = "";

    @Override
    public Exception decode(String s, Response response) {
        try {
            log.error("Error occurred in Feign {} ", response.request().url());
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            JsonNode jsonNode = mapper.readTree(response.body().asReader());

            final String responseErrorMessage = getJsonValueAsText(jsonNode, MESSAGE).orElse(EMPTY_STRING);
            final String responseErrorCode = getJsonValueAsText(jsonNode, ERROR_CODE)
                    .orElse(getJsonValueAsText(jsonNode, ERROR_ID).orElse(EMPTY_STRING));
            final String responseErrorDetails = getJsonValueAsText(jsonNode, ERROR_DETAIL).orElse(EMPTY_STRING);

            log.error("Error Response from Feign [{}] error-code [{}] error-detail [{}] error-message{} ",
                    response.request().url(),
                    responseErrorCode,
                    responseErrorDetails,
                    responseErrorMessage);

            final String errorDetails = appendToErrorDetails(responseErrorDetails, responseErrorCode);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR, EXTERNAL_SERVICE_ERROR.getErrorMessage(), errorDetails);

            //This line will to throw Runtime Exception will never get executed as the GenericExceptionHandler will throw the exception.
            // Adding this line as because of Feign framework
            //TODO Discuss with ms-squad team to given a way to throw exception from microservice
            return new RuntimeException(responseErrorMessage + " " + responseErrorCode + " " + responseErrorDetails);

        } catch (IOException e) {
            // Adding this catch block but ideally this should never
            // as it is only for asReader() call
            log.error("Error while reading Feign Error Response {} ", e);
            return new RuntimeException(e);
        }
    }

    private String appendToErrorDetails(String responseErrorDetails, String responseErrorCode) {
        return StringUtils.isBlank(responseErrorDetails)
                ? responseErrorCode
                : responseErrorDetails + "," + responseErrorCode;
    }

    private Optional<String> getJsonValueAsText(JsonNode jsonNode, String code) {
        return Optional.ofNullable(jsonNode.get(code))
                .map(JsonNode::asText);
    }

}
