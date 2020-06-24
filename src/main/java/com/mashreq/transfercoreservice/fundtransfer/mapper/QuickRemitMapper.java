package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.transfercoreservice.fundtransfer.dto.*;

/**
 * @author shahbazkh
 * @date 5/5/20
 */
@FunctionalInterface
public interface QuickRemitMapper {

    QuickRemitFundTransferRequest mapTo(FundTransferMetadata metadata, FundTransferRequestDTO request, FundTransferContext context);
}