package com.mashreq.transfercoreservice.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class BICCodeSearchRequestDto {
    private String countryCode;
    private String searchType;
    private String value;
}
