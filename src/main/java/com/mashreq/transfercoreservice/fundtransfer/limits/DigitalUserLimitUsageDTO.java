package com.mashreq.transfercoreservice.fundtransfer.limits;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DigitalUserLimitUsageDTO {

    private Long digitalUserId;
    private String cif;
    private String channel;
    private String beneficiaryTypeCode;
    private BigDecimal paidAmount;
    private String versionUuid;
    private String createdBy;
    private String transactionRefNo;
    private Long beneficiaryId;
}
