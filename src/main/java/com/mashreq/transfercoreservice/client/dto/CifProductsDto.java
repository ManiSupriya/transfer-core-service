package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Data
public class CifProductsDto {
    private List<SearchAccountDto> accounts;
}