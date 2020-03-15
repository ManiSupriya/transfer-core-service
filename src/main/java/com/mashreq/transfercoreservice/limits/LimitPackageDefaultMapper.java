package com.mashreq.transfercoreservice.limits;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LimitPackageDefaultMapper {

    LimitDTO limitDtoFromEntity(LimitPackageDefault limitPackageDefault);
}


