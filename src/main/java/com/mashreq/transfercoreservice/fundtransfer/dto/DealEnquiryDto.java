package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString(callSuper = true)
public class DealEnquiryDto {
    @NotBlank
    private final String dealNo;
    private Set<DealEnquiryDetailsDto> detailsDtoList = new HashSet<>();

    public void addDealEnquiryDetails(Set<DealEnquiryDetailsDto> detailsDto) {
        detailsDtoList.addAll(detailsDto);
    }
}
