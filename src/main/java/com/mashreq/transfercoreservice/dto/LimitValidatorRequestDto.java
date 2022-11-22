package com.mashreq.transfercoreservice.dto;

import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LimitValidatorRequestDto {
    private UserDTO userDTO;
    private String beneficiaryType;
    private  BigDecimal paidAmount;
    private Long beneId;
}
