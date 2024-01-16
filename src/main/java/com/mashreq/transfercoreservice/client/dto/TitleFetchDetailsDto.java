package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by KrishnaKo on 05/01/2024
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TitleFetchDetailsDto {
    @JsonProperty("responseDetails")
    private TitleFetchDetails  titleFetchDetails;

}