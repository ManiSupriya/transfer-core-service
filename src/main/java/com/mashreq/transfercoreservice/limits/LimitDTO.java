package com.mashreq.transfercoreservice.limits;

import com.mashreq.transfercoreservice.fundtransfer.dto.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.model.DigitalUserLimitUsage;
import lombok.Data;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Data
public class LimitDTO {
    private BigDecimal maxAmountDaily;
    private BigDecimal maxAmountMonthly;
    private BigDecimal maxTrxAmount;
    private Integer maxCountDaily;
    private Integer maxCountMonthly;
    private String versionUuid;

    /**
     * @Author KetulkumarS
     */
    @Mapper(componentModel = "spring")
    public static interface LimitPackageDefaultMapper {
        LimitDTO limitDtoFromEntity(LimitPackageDefault limitPackageDefault);
    }

    /**
     * @Author KetulkumarS
     */
    @Mapper(componentModel = "spring")
    public static interface DigitalUserLimitUsageMapper {
        DigitalUserLimitUsage userLimitUsageDTOToEntity(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO);
    }
}
