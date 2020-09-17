package com.mashreq.transfercoreservice.banksearch;

import org.mapstruct.Mapper;

import com.mashreq.transfercoreservice.client.dto.CoreBankDetails;

@Mapper(componentModel = "spring")
public interface SwiftBankDetailsMapper {

	BankResultsDto coreBankResultsToDto(CoreBankDetails bankDetails);
}
