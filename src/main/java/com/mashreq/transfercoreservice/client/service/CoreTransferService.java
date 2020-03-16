package com.mashreq.transfercoreservice.client.service;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.CoreTransferClient;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.FundTransferMWResponse;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.errors.FundTransferException;
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
        try {
            Response<FundTransferMWResponse> transferFundsResponse =
                    coreTransferClient.transferFundsBetweenAccounts(coreFundTransferRequestDto);

            log.info("Response for fund-transfer call {} ", transferFundsResponse);

            return CoreFundTransferResponseDto.builder()
                    .transactionRefNo(transferFundsResponse.getData().getTransactionRefNo())
                    .mwReferenceNo(transferFundsResponse.getData().getMwReferenceNo())
                    .mwResponseStatus(transferFundsResponse.getData().getMwResponseStatus())
                    .mwResponseCode(transferFundsResponse.getData().getMwResponseCode())
                    .mwResponseDescription(transferFundsResponse.getData().getMwResponseDescription())
                    .build();

        } catch (FundTransferException e) {
            log.debug("Fund Transfer From account {}, to account {} failed ",
                    coreFundTransferRequestDto.getFromAccount(), coreFundTransferRequestDto.getToAccount());

            return CoreFundTransferResponseDto.builder()
                    .mwResponseStatus(MwResponseStatus.F)
                    .transferErrorCode(e.getTransferErrorCode())
                    .externalErrorMessage(e.getTransferErrorCode().getErrorMessage() + " -- " + e.getMessage())
                    .build();
        }

    }
}
