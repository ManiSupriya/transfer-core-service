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

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransferLimitRequestDto {
    private Long beneficiaryId;
    private FTOrderType orderType;
    private TransferType transferType;
    @NotBlank
    private BigDecimal amount;
    @NotBlank
    private String accountCurrency;
    @NotBlank
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