package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by KrishnaKo on 05/01/2024
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UaeIbanTitleFetchRequest {

    @JsonProperty("WebServiceData")
    private List<IbanDto> ibanList;
}
