package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class DealEnquiryDto {
    @NotBlank
    private String dealNo;
    private Set<DealEnquiryDetailsDto> detailsDtoList = new HashSet<>();

    public void addDealEnquiryDetails(Set<DealEnquiryDetailsDto> detailsDto) {
        detailsDtoList.addAll(detailsDto);
    }
}
