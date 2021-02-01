package com.mashreq.transfercoreservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/22/20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryResponseDto {

    private List<CountryDto> trending;
    private List<CountryDto> allCountries;
}
