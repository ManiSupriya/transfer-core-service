package com.mashreq.transfercoreservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.StringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EXTERNAL_SERVICE_ERROR;

/**
 * @author shahbazkh
 * @date 3/31/20
 */

@Slf4j
public class FeignDecoder implements Decoder {
    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        try {
            log.info("Feign Response for {} ", response.request().url());
            ObjectMapper mapper = new ObjectMapper();
            FeignResponse feignResponse = mapper.readValue(response.body().asReader(), FeignResponse.class);
            log.info("Feign Response {} ", feignResponse);
            return feignResponse;

        } catch (IOException e) {
            // Adding this catch block but ideally this should never
            // as it is only for asReader() call
            log.error("Error while reading Feign Error Response {} ", e);
            return new RuntimeException(e);
        }
    }
}
