package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferServiceImpl implements FundTransferService {

    private final CoreTransferService coreTransferService;

    /**
     * TODO
     *  validate serviceType
     *
     */
    public PaymentHistoryDTO transferFund(FundTransferRequestDTO fundTransferRequestDTO) {

        CoreFundTransferRequestDto coreFundTransferRequestDto = CoreFundTransferRequestDto.builder()
                .fromAccount(fundTransferRequestDTO.getFromAccount())
                .toAccount(fundTransferRequestDTO.getToAccount())
                .amount(fundTransferRequestDTO.getAmount())
                .currency(fundTransferRequestDTO.getCurrency())
                .dealNumber(fundTransferRequestDTO.getDealNumber())
                .purposeCode(fundTransferRequestDTO.getPurposeCode())
                .build();

        CoreFundTransferResponseDto coreResp = coreTransferService.transferFundsBetweenAccounts(coreFundTransferRequestDto);

        return PaymentHistoryDTO.builder().build();
    }
}
