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
    private final RoutingCodeSearchMWService routingCodeSearchMWService;
    private final IfscCodeSearchMWService ifscCodeSearchMWService;


    public List<BankResultsDto> getBankDetails(final String channelTraceId, final BankDetailRequestDto bankDetailRequest) {
        if ("iban".equals(bankDetailRequest.getType())) {
            return ibanSearchMWService.fetchBankDetailsWithIban(channelTraceId, bankDetailRequest.getValue());
        }
        return routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(channelTraceId, bankDetailRequest);
    }

    public BankResultsDto getBankDeatilsByIfsc(final String channelTraceId, final String ifscCode) {
        return ifscCodeSearchMWService.getBankDetailByIfscCode(channelTraceId, ifscCode);
    }

}
