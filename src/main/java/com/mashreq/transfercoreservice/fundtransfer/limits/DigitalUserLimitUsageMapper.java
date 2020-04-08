package com.mashreq.transfercoreservice.limits;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DigitalUserLimitUsageMapper {

    DigitalUserLimitUsage userLimitUsageDTOToEntity(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO);
}
