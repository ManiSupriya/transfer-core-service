package com.mashreq.transfercoreservice.config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;

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
