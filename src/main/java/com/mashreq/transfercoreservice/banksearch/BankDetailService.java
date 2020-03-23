package com.mashreq.transfercoreservice.banksearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BankDetailService {

    private final IbanSearchMWService ibanSearchMWService;

    public BankResultsDto getBankDetails(String channelTraceId, BankDetailRequestDto bankDetailRequest) {
        BankResultsDto bankResultsDto = ibanSearchMWService.fetchBankDetailsWithIban(channelTraceId, bankDetailRequest.getValue());
        return bankResultsDto;
    }

}
