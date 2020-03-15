package com.mashreq.transfercoreservice.limits;

import com.mashreq.transfercoreservice.fundtransfer.dto.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.model.DigitalUserLimitUsage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitDTO {
    private BigDecimal maxAmountDaily;
    private BigDecimal maxAmountMonthly;
    private BigDecimal maxTrxAmount;
    private Integer maxCountDaily;
    private Integer maxCountMonthly;
    private String versionUuid;


}
