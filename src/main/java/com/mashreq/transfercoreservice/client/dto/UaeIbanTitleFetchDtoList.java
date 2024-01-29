package com.mashreq.transfercoreservice.client.dto;

/**
 * Created by KrishnaKo on 05/01/2024
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UaeIbanTitleFetchDtoList {
    @JsonProperty("Response")
    private List<TitleFetchDetailsDto> titleFetchDetailsDtos;
}
