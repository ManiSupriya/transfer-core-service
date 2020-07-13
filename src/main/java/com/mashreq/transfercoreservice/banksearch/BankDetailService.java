package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.mobcommons.services.http.RequestMetaData;
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


    public List<BankResultsDto> getBankDetails(final String channelTraceId, final BankDetailRequestDto bankDetailRequest, final RequestMetaData requestMetaData) {
        if ("iban".equals(bankDetailRequest.getType())) {
            return ibanSearchMWService.fetchBankDetailsWithIban(channelTraceId, bankDetailRequest.getValue(), requestMetaData );
        }
        return routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(channelTraceId, bankDetailRequest, requestMetaData);
    }

    public BankResultsDto getBankDeatilsByIfsc(final String channelTraceId, final String ifscCode, final RequestMetaData requestMetaData) {
        return ifscCodeSearchMWService.getBankDetailByIfscCode(channelTraceId, ifscCode, requestMetaData);
    }

}
