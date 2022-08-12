package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.transfercoreservice.model.TransferLimit;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferLimitRequestDto {
    private Long beneficiaryId;
    private FTOrderType orderType;
    private TransferType transferType;
    private BigDecimal amount;

    public TransferLimit toEntity() {
        TransferLimit limit = new TransferLimit();
        limit.setBeneficiaryId(this.beneficiaryId);
        limit.setAmount(this.amount);
        limit.setOrderType(this.orderType);
        limit.setTransferType(this.transferType);
        return limit;
    }
}