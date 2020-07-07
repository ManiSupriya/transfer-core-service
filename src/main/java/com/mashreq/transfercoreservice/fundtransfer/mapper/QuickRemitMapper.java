package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.mobcommons.config.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;

/**
 * @author shahbazkh
 * @date 5/5/20
 */
@FunctionalInterface
public interface QuickRemitMapper {

    QuickRemitFundTransferRequest mapTo(RequestMetaData metadata, FundTransferRequestDTO request, FundTransferContext context);
}
