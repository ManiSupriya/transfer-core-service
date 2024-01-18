package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.transfercoreservice.client.OmwExternalClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.config.feign.OmwExternalConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by KrishnaKo on 08/01/2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UAEAccountTitleFetchService {
    private final OmwExternalClient omwExternalClient;
    private final OmwExternalConfigProperties omwExternalConfigProperties;

    public String fetchAccountTitle(String iban){
       try {
        UaeIbanTitleFetchResponse  uaeIbanTitleFetchResponse = omwExternalClient.getAccountTitle(prepareTitleFetchRequest(iban), omwExternalConfigProperties.getServiceId());
           return fetchTitleFromResponse(uaeIbanTitleFetchResponse);
       }
       catch(Exception ex){
           log.error("Error while doing title fetch ",ex);
           return EMPTY;
       }
    }

    private UaeIbanTitleFetchRequest prepareTitleFetchRequest(String iban){
        return new UaeIbanTitleFetchRequest(Collections.singletonList(new IbanRecord(iban)));
    }

    private String fetchTitleFromResponse(UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse){
        return Optional.ofNullable(uaeIbanTitleFetchResponse)
                .map(UaeIbanTitleFetchResponse::getUaeIbanTitleFetch)
                .map(UaeIbanTitleFetchDto::getUaeIbanTitleFetchDtoList)
                .map(UaeIbanTitleFetchDtoList::getTitleFetchDetailsDtos)
                .flatMap(t -> t.stream().findFirst())
                .map(TitleFetchDetailsDto::getTitleFetchDetails)
                .map(TitleFetchDetails::getTitleFetchDto)
                .map(TitleFetchDto::getTitle)
                .orElse(EMPTY);
    }
}
