package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.OwnAccountStrategy;
import com.mashreq.transfercoreservice.fundtransfer.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.strategy.WithinMashreqStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;

import static com.mashreq.transfercoreservice.fundtransfer.ServiceType.OWN_ACCOUNT;
import static com.mashreq.transfercoreservice.fundtransfer.ServiceType.WITHIN_MASHREQ;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferServiceDefault implements FundTransferService {

    private final CoreTransferService coreTransferService;
//    private final OwnAccountStrategy ownAccountStrategy;
//    private final WithinMashreqStrategy withinMashreqStrategy;
//    private EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;
//
//    @PostConstruct
//    public void init() {
//        fundTransferStrategies = new EnumMap<>(ServiceType.class);
//        fundTransferStrategies.put(OWN_ACCOUNT, ownAccountStrategy);
//        fundTransferStrategies.put(WITHIN_MASHREQ, withinMashreqStrategy);
//    }

    /**
     * TODO
     * validate serviceType
     */
    public CoreFundTransferResponseDto transferFund(FundTransferRequestDTO fundTransferRequestDTO) {

        CoreFundTransferRequestDto coreFundTransferRequestDto = CoreFundTransferRequestDto.builder()
                .fromAccount(fundTransferRequestDTO.getFromAccount())
                .toAccount(fundTransferRequestDTO.getToAccount())
                .amount(fundTransferRequestDTO.getAmount())
                .currency(fundTransferRequestDTO.getCurrency())
                .dealNumber(fundTransferRequestDTO.getDealNumber())
                .purposeCode(fundTransferRequestDTO.getPurposeCode())
                .build();

        CoreFundTransferResponseDto coreResp = coreTransferService.transferFundsBetweenAccounts(coreFundTransferRequestDto);

        return coreResp;
    }
}
