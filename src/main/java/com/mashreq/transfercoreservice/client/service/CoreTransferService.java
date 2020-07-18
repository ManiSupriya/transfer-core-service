package com.mashreq.transfercoreservice.client.service;

import com.mashreq.transfercoreservice.client.CoreTransferClient;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.FundTransferMWResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
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
        CoreFundTransferRequestDto coreFundTransferRequestDto = new CoreFundTransferRequestDto();
        coreFundTransferRequestDto.setFromAccount(request.getFromAccount());
        coreFundTransferRequestDto.setToAccount(request.getToAccount());
        coreFundTransferRequestDto.setAmount(request.getAmount());
        coreFundTransferRequestDto.setCurrency(request.getCurrency());
        coreFundTransferRequestDto.setDealNumber(request.getDealNumber());
        coreFundTransferRequestDto.setPurposeCode(request.getPurposeCode());
        log.info("Calling external service for fundtransfer {} ", coreFundTransferRequestDto);
        return transferFundsBetweenAccounts(coreFundTransferRequestDto);
    }

    /**
     * Fund Transfer
     */
    private FundTransferResponse transferFundsBetweenAccounts(CoreFundTransferRequestDto coreFundTransferRequestDto) {
        final FundTransferResponse result = FundTransferResponse.builder().build();
        Response<FundTransferMWResponse> transferFundsResponse =
                coreTransferClient.transferFundsBetweenAccounts(coreFundTransferRequestDto);

        log.info("Response for fund-transfer call {} ", transferFundsResponse);
        final CoreFundTransferResponseDto coreResponse = new CoreFundTransferResponseDto();
        coreResponse.setTransactionRefNo(transferFundsResponse.getData().getTransactionRefNo());
        coreResponse.setMwReferenceNo(transferFundsResponse.getData().getMwReferenceNo());
        coreResponse.setMwResponseStatus(transferFundsResponse.getData().getMwResponseStatus());
        coreResponse.setMwResponseCode(transferFundsResponse.getData().getMwResponseCode());
        coreResponse.setMwResponseDescription(transferFundsResponse.getData().getMwResponseDescription());

        return result.toBuilder().responseDto(coreResponse).build();
    }
}
