package com.mashreq.transfercoreservice.banksearch;

import org.mapstruct.Mapper;
import org.springframework.context.annotation.Primary;

import com.mashreq.transfercoreservice.client.dto.CoreBankDetails;

@Primary
@Mapper(componentModel = "spring")
public interface SwiftBankDetailsMapper {

	BankResultsDto coreBankResultsToDto(CoreBankDetails bankDetails);
}
