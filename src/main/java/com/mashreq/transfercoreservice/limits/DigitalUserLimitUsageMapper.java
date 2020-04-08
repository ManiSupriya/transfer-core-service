package com.mashreq.transfercoreservice.limits;

import com.mashreq.transfercoreservice.fundtransfer.dto.DigitalUserLimitUsageDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DigitalUserLimitUsageMapper {

    DigitalUserLimitUsage userLimitUsageDTOToEntity(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO);
}
