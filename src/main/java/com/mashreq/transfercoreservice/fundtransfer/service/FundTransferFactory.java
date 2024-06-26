package com.mashreq.transfercoreservice.fundtransfer.service;

import org.springframework.stereotype.Service;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FundTransferFactory {
	private final PayLaterTransferService payLaterTransferService;
	/** named on a sense that SI and pay later transactions are executed later only */
	private final FundTransferServiceDefault payNowService;
	
	public FundTransferService getServiceAppropriateService(FundTransferRequestDTO request) {
		FTOrderType orderType = FTOrderType.getFTOrderTypeByName(request.getOrderType());
		if(FTOrderType.PN.equals(orderType)) {
			return payNowService;
		}
		return payLaterTransferService;
	}
}
