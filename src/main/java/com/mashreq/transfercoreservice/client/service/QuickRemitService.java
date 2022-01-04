package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.QUICK_REMIT_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.Optional;

import com.mashreq.transfercoreservice.client.dto.CountryDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.QuickRemitServiceClient;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.QRExchangeRequest;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickRemitService {

	private final QuickRemitServiceClient quickRemitServiceClient;
	private final AsyncUserEventPublisher userEventPublisher;

	public QRExchangeResponse exchange(FundTransferEligibiltyRequestDTO request, CountryDto countryDto, RequestMetaData metaData) {
		Response<QRExchangeResponse> quickRemitResponse = quickRemitServiceClient.exchange(
				QRExchangeRequest.builder()
				.benId(Long.valueOf(request.getBeneficiaryId()))
				.destinationCcy(countryDto.getNativeCurrency())
				.initiatedFrom("QR")
				.senderAcNum(
						StringUtils.isNotBlank(request.getCardNo()) ?
								request.getCardNo() : request.getFromAccount())
				.sourceCcy(request.getCurrency())
				.transactionAmt(request.getAmount())
				.transactionCcy(request.getTxnCurrency())
				.build());

		if (ResponseStatus.ERROR == quickRemitResponse.getStatus() || isNull(quickRemitResponse.getData())) {
			userEventPublisher.publishFailureEvent(FundTransferEventType.ELIGIBILITY_QUICK_REMIT_EXCHANGE, metaData, "failed to get quick remit eligibility details",
					quickRemitResponse.getErrorCode(), quickRemitResponse.getMessage(), quickRemitResponse.getMessage());
			GenericExceptionHandler.handleError(QUICK_REMIT_EXTERNAL_SERVICE_ERROR, QUICK_REMIT_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
					getErrorDetails(quickRemitResponse));
		}
		return quickRemitResponse.getData();
	}

    public static String getErrorDetails(Response response) {
        if (StringUtils.isNotBlank(response.getErrorDetails())) {
            return response.getErrorCode() + "," + response.getErrorDetails() + "," + response.getMessage();
        }
        return response.getErrorCode() + "," + response.getMessage();
    }

}
