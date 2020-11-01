package com.mashreq.transfercoreservice.fundtransfer.limits;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DigitalUserLimitUsageDTO {

    private Long digitalUserId;
    private String cif;
    private String channel;
    private String beneficiaryTypeCode;
    private BigDecimal paidAmount;
    private String versionUuid;
    private String createdBy;
    private String transactionRefNo;
    private Long beneId;
}
