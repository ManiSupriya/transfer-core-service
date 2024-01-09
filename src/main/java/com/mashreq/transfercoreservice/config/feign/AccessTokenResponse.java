package com.mashreq.transfercoreservice.config.feign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by KrishnaKo on 08/01/2024
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessTokenResponse implements Serializable {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private String expiresIn;

    @Override
    public String toString() {
        return "AccessTokenResponseDto{" +
                "accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
