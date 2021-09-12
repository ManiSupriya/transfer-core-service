package com.mashreq.transfercoreservice.banksearch;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BIC_SEARCH_FAILED;
import static java.util.Objects.isNull;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryServiceClient;
import com.mashreq.transfercoreservice.client.dto.BICCodeSearchRequestDto;
import com.mashreq.transfercoreservice.client.dto.BICCodeSearchResponseDto;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BICCodeSearchService {

    private final BeneficiaryServiceClient beneficiaryServiceClient;
    private final AsyncUserEventPublisher userEventPublisher;
    private static final String SEARCHTYPE = "BIC";
    private static final String SEARCHVAL = "ANY";

    public List<BankResultsDto> fetchBankDetailsWithBic(final String countryCode,
                                                        final RequestMetaData requestMetaData) {

        log.info("Searching for Bank details with BIC for country [ {} ]", htmlEscape(countryCode));
        BICCodeSearchRequestDto bicCodeSearchRequestDto = BICCodeSearchRequestDto.builder()
                .countryCode(countryCode)
                .searchType(SEARCHTYPE)
                .value(SEARCHVAL)
                .build();

        Response<List<BICCodeSearchResponseDto>> response = beneficiaryServiceClient.fetchBankDetailsWithBic(bicCodeSearchRequestDto);
        if(isNull(response)){
            userEventPublisher.publishFailureEvent(FundTransferEventType.BIC_LIST_SEARCH_CALL, requestMetaData,
                    "failed to get bank list by BIC ", null, null, null);
            GenericExceptionHandler.handleError(BIC_SEARCH_FAILED, BIC_SEARCH_FAILED.getErrorMessage(),
                    BIC_SEARCH_FAILED.getErrorMessage());
        }

        if ( ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
                userEventPublisher.publishFailureEvent(FundTransferEventType.BIC_LIST_SEARCH_CALL, requestMetaData,
                        "failed to get bank list by BIC ", response.getMessage(), response.getMessage(), response.getMessage());
                GenericExceptionHandler.handleError(BIC_SEARCH_FAILED, BIC_SEARCH_FAILED.getErrorMessage(),
                        response.getMessage());
        }
        log.info("Banks fetched successfully for country = {}", htmlEscape(countryCode));
        List<BICCodeSearchResponseDto> responseDtoList = response.getData();
        return responseDtoList.stream().map(BankResultsDto::new).collect(Collectors.toList());

    }
}
