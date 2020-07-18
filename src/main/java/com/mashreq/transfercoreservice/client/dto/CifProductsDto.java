package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Data
@NoArgsConstructor
public class CifProductsDto {
    private List<SearchAccountDto> accounts;
}