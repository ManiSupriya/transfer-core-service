package com.mashreq.transfercoreservice.fundtransfer.limits;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DigitalUserLimitUsageMapper {

    DigitalUserLimitUsage userLimitUsageDTOToEntity(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO);
}
