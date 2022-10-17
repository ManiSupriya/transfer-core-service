package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.transfercoreservice.dto.BankResolverRequestDto;

import java.util.List;

public interface BankDetailsResolver {

    List<BankResultsDto> getBankDetails(BankResolverRequestDto bankResolverRequestDto);
}
