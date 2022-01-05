package com.mashreq.transfercoreservice.fundtransfer.duplicateRequestValidation;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.mashreq.dedupe.dto.DedupeRequestDto;
import com.mashreq.dedupe.resolver.UniqueRequestResolver;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FundsTransferRequestResolver implements UniqueRequestResolver<FundTransferRequestDTO> {

	@Override
	public DedupeRequestDto resolveUniqueRequest(FundTransferRequestDTO request) {
		log.debug("inside FundsTransferRequestResolver.resolveUniqueRequest");
		Objects.requireNonNull(request);
		Objects.requireNonNull(request.getFinTxnNo());
		DedupeRequestDto dedupeRequestDto = new DedupeRequestDto(false, request.getFinTxnNo(),
				TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST.customErrorCode(),
				TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST.getErrorMessage());
		log.debug("dedupe request successfully created");
		return dedupeRequestDto;
	}

}
