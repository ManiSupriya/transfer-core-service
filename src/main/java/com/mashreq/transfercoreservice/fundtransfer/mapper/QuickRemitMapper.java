package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferContext;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;

/**
 * @author shahbazkh
 * @date 5/5/20
 */
@FunctionalInterface
public interface QuickRemitMapper {

    QuickRemitFundTransferRequest mapTo(RequestMetaData metadata, FundTransferRequestDTO request, FundTransferContext context);
}
