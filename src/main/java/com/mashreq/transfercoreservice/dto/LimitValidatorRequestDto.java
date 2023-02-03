package com.mashreq.transfercoreservice.dto;

import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitValidatorRequestDto {
    private UserDTO userDTO;
    private String beneficiaryType;
    private  BigDecimal paidAmount;
    private Long beneId;
}
