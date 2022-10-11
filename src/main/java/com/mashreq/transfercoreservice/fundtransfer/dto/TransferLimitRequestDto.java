package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransferLimitRequestDto {
    private Long beneficiaryId;
    private FTOrderType orderType;
    private TransferType transferType;
    private BigDecimal amount;
    private String accountCurrency;
    private String accountNumber;

    public TransferLimit toEntity(String transactionRefNo) {
        TransferLimit limit = new TransferLimit();
        limit.setBeneficiaryId(this.beneficiaryId);
        limit.setAmount(this.amount);
        limit.setOrderType(this.orderType);
        limit.setTransferType(this.transferType);
        limit.setTransactionRefNo(transactionRefNo);
        return limit;
    }
}