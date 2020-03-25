package com.mashreq.transfercoreservice.client.service;

import com.mashreq.transfercoreservice.client.CoreTransferClient;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.FundTransferMWResponse;
import com.mashreq.transfercoreservice.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.errors.FundTransferException;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreTransferService {

    private final CoreTransferClient coreTransferClient;
    private final WebServiceClient webServiceClient;


    public FundTransferResponse transferFundsBetweenAccounts(FundTransferRequestDTO request) {

        CoreFundTransferRequestDto coreFundTransferRequestDto = CoreFundTransferRequestDto.builder()
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .dealNumber(request.getDealNumber())
                .purposeCode(request.getPurposeCode())
                .build();

        log.info("Calling external service for fundtransfer {} ", coreFundTransferRequestDto);
        return transferFundsBetweenAccounts(coreFundTransferRequestDto);
    }

    /**
     * Fund Transfer
     */
    private FundTransferResponse transferFundsBetweenAccounts(CoreFundTransferRequestDto coreFundTransferRequestDto) {
        final FundTransferResponse result = FundTransferResponse.builder().build();
        try {
            Response<FundTransferMWResponse> transferFundsResponse =
                    coreTransferClient.transferFundsBetweenAccounts(coreFundTransferRequestDto);

            log.info("Response for fund-transfer call {} ", transferFundsResponse);
            final CoreFundTransferResponseDto coreResponse = CoreFundTransferResponseDto.builder()
                    .transactionRefNo(transferFundsResponse.getData().getTransactionRefNo())
                    .mwReferenceNo(transferFundsResponse.getData().getMwReferenceNo())
                    .mwResponseStatus(transferFundsResponse.getData().getMwResponseStatus())
                    .mwResponseCode(transferFundsResponse.getData().getMwResponseCode())
                    .mwResponseDescription(transferFundsResponse.getData().getMwResponseDescription())
                    .build();
            return result.toBuilder().responseDto(coreResponse).build();

        } catch (FundTransferException e) {
            log.error("Fund Transfer From account {}, to account {} failed",
                    coreFundTransferRequestDto.getFromAccount(), coreFundTransferRequestDto.getToAccount());
            log.error("Error {} ", e);
            final CoreFundTransferResponseDto coreResponse = CoreFundTransferResponseDto.builder()
                    .mwResponseStatus(MwResponseStatus.F)
                    .transferErrorCode(e.getTransferErrorCode())
                    .externalErrorMessage(e.getTransferErrorCode().getErrorMessage())
                    .build();

            return result.toBuilder().responseDto(coreResponse).build();
        }

    }
}
