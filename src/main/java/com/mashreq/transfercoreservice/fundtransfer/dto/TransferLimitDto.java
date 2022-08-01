package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.transfercoreservice.model.TransferLimit;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransferLimitDto {
    private Long beneficiaryId;
    private FTOrderType orderType;
    private BigDecimal amount;

    public TransferLimit toEntity() {
        TransferLimit limit = new TransferLimit();
        limit.setBeneficiaryId(this.beneficiaryId);
        limit.setAmount(this.amount);
        limit.setOrderType(this.orderType);
        return limit;
    }
}