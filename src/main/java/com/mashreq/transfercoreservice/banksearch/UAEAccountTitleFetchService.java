package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.transfercoreservice.client.OmwExternalClient;
import com.mashreq.transfercoreservice.client.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by KrishnaKo on 08/01/2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UAEAccountTitleFetchService {
    private final OmwExternalClient omwExternalClient;

    public String getAccountTitle(String iban){
       try {
        UaeIbanTitleFetchResponse  uaeIbanTitleFetchResponse = omwExternalClient.getAccountTitle(prepareTitleFetchRequest(iban), "EFTS0001");
           return fetchTitleFromResponse(uaeIbanTitleFetchResponse);
       }
       catch(Exception ex){
           log.error("Error while doing title fetch ",ex);
           return EMPTY;
       }
    }

    private UaeIbanTitleFetchRequest prepareTitleFetchRequest(String iban){
        return UaeIbanTitleFetchRequest.builder().ibanList(Collections.singletonList(getIban(iban))).build();
    }

    private IbanDto getIban(String iban){
        return IbanDto.builder().iban(iban).build();
    }
    private String fetchTitleFromResponse(UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse){
        if(nonNull(uaeIbanTitleFetchResponse)) {
            UaeIbanTitleFetchDto uaeIbanTitleFetchDto = uaeIbanTitleFetchResponse.getUaeIbanTitleFetch();
            if (nonNull(uaeIbanTitleFetchDto) &&
                    nonNull(uaeIbanTitleFetchDto.getUaeIbanTitleFetchDtoList()) &&
                    isNotEmpty(uaeIbanTitleFetchDto.getUaeIbanTitleFetchDtoList().getTitleFetchDetailsDtos())) {
                List<TitleFetchDetailsDto> titleFetchDetailsDtoList = uaeIbanTitleFetchDto.getUaeIbanTitleFetchDtoList().getTitleFetchDetailsDtos();
                TitleFetchDetailsDto titleFetchDetailsDto = titleFetchDetailsDtoList.get(0);
                if (nonNull(titleFetchDetailsDto)) {
                    TitleFetchDetails titleFetchDetails = titleFetchDetailsDto.getTitleFetchDetails();
                    if (nonNull(titleFetchDetails)) {
                        TitleFetchDto titleFetchDto = titleFetchDetails.getTitleFetchDto();
                        if (nonNull(titleFetchDto)) {
                            return titleFetchDto.getTitle();
                        }
                    }
                }
            }
        }
        return EMPTY;
    }
}
