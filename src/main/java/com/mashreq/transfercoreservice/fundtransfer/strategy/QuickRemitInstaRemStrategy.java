package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.mapper.QuickRemitInstaRemRequestMapper;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author shahbazkh
 * @date 5/5/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickRemitInstaRemStrategy implements QuickRemitFundTransfer {

    private final QuickRemitInstaRemRequestMapper mapper;
    private final QuickRemitFundTransferMWService quickRemitFundTransferMWService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO, ValidationContext context) {

        log.info("Quick Remit InstaRem initiated ");

//        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = mapper.map(
//                "",
//                "",
//                "",
//                "",
//                "",
//                "",
//                ""
//        );

        quickRemitFundTransferMWService.transfer(null);

        return null;
    }
}