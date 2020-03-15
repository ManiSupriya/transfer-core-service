package com.mashreq.transfercoreservice.client.service;

import com.mashreq.transfercoreservice.client.CoreTransferClient;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.webcore.dto.response.Response;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreTransferService {

    private final CoreTransferClient coreTransferClient;

    /**
     * Fund Transfer
     */
    public CoreFundTransferResponseDto transferFundsBetweenAccounts(CoreFundTransferRequestDto coreFundTransferRequestDto) {

        CoreFundTransferResponseDto coreFundTransferResponseDto = CoreFundTransferResponseDto.builder().build();

        try {
            Response<String> transferFundsResponse =
                    coreTransferClient.transferFundsBetweenAccounts(coreFundTransferRequestDto);
            coreFundTransferResponseDto.setTransactionRefNo(transferFundsResponse.getData());
            coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);

        } catch (FeignException e) {
            log.debug("Fund Transfer From account {}, to account {} failed ",
                    coreFundTransferRequestDto.getFromAccount(), coreFundTransferRequestDto.getToAccount());
            coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.F);
        }

        return coreFundTransferResponseDto;
    }
}
