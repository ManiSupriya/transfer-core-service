package com.mashreq.transfercoreservice.banksearch;


import com.mashreq.transfercoreservice.client.OmwExternalClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.config.feign.OmwExternalConfigProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UAEAccountTitleFetchServiceTest {

    @InjectMocks
    public UAEAccountTitleFetchService uaeAccountTitleFetchService;
    @Mock
    private OmwExternalClient omwExternalClient;
    @Mock
    private OmwExternalConfigProperties omwExternalConfigProperties;


    @Test
    public void testGetAccountTitle() {
        //Given
        UaeIbanTitleFetchResponse  uaeIbanTitleFetchResponse = buildUaeIbanTitleFetchResponse();
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("ACCOUNT NUSRDN", response);

    }

    @Test
    public void testAccountGetTitleException() {
        //Given
        UaeIbanTitleFetchResponse  uaeIbanTitleFetchResponse = buildUaeIbanTitleFetchResponse();
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenThrow(new RuntimeException("Error"));
        //then
        assertEquals("",uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108"));

    }

    @Test
    public void testAccountGetTitleResponseEmpty() {

        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(null);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }

    @Test
    public void testAccountGetTitleUaeIbanTitleFetchResponse() {
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }

    @Test
    public void testAccountGetTitleUaeIbanTitleFetchDto() {
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        UaeIbanTitleFetchDto uaeIbanTitleFetchDto = new UaeIbanTitleFetchDto();
        uaeIbanTitleFetchResponse.setUaeIbanTitleFetch(uaeIbanTitleFetchDto);
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }

    @Test
    public void testAccountGetTitleUaeIbanTitleFetchDtoList() {
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        UaeIbanTitleFetchDto uaeIbanTitleFetchDto = new UaeIbanTitleFetchDto();
        UaeIbanTitleFetchDtoList uaeIbanTitleFetchDtoList = new UaeIbanTitleFetchDtoList();
        uaeIbanTitleFetchDto.setUaeIbanTitleFetchDtoList(uaeIbanTitleFetchDtoList);
        uaeIbanTitleFetchResponse.setUaeIbanTitleFetch(uaeIbanTitleFetchDto);
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }

    @Test
    public void testAccountGetTitleUaeTitleFetchDetailsDtosList() {
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        UaeIbanTitleFetchDto uaeIbanTitleFetchDto = new UaeIbanTitleFetchDto();
        UaeIbanTitleFetchDtoList uaeIbanTitleFetchDtoList = new UaeIbanTitleFetchDtoList();
        List<TitleFetchDetailsDto> titleFetchDetailsDtos = new ArrayList<>();
        uaeIbanTitleFetchDtoList.setTitleFetchDetailsDtos(titleFetchDetailsDtos);
        uaeIbanTitleFetchDto.setUaeIbanTitleFetchDtoList(uaeIbanTitleFetchDtoList);
        uaeIbanTitleFetchResponse.setUaeIbanTitleFetch(uaeIbanTitleFetchDto);
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }

    @Test
    public void testAccountGetTitleUaeTitleFetchDetailsDtos() {
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        UaeIbanTitleFetchDto uaeIbanTitleFetchDto = new UaeIbanTitleFetchDto();
        UaeIbanTitleFetchDtoList uaeIbanTitleFetchDtoList = new UaeIbanTitleFetchDtoList();
        List<TitleFetchDetailsDto> titleFetchDetailsDtos = new ArrayList<>();
        TitleFetchDetailsDto titleFetchDetailsDto = new TitleFetchDetailsDto();
        titleFetchDetailsDtos.add(titleFetchDetailsDto);
        uaeIbanTitleFetchDtoList.setTitleFetchDetailsDtos(titleFetchDetailsDtos);
        uaeIbanTitleFetchDto.setUaeIbanTitleFetchDtoList(uaeIbanTitleFetchDtoList);
        uaeIbanTitleFetchResponse.setUaeIbanTitleFetch(uaeIbanTitleFetchDto);
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }

    @Test public void testAccountGetTitleUaeTitleFetchDetails() {
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        UaeIbanTitleFetchDto uaeIbanTitleFetchDto = new UaeIbanTitleFetchDto();
        UaeIbanTitleFetchDtoList uaeIbanTitleFetchDtoList = new UaeIbanTitleFetchDtoList();
        List<TitleFetchDetailsDto> titleFetchDetailsDtos = new ArrayList<>();
        TitleFetchDetailsDto titleFetchDetailsDto = new TitleFetchDetailsDto();
        TitleFetchDetails titleFetchDetails = new TitleFetchDetails();
        titleFetchDetailsDto.setTitleFetchDetails(titleFetchDetails);
        titleFetchDetailsDtos.add(titleFetchDetailsDto);
        uaeIbanTitleFetchDtoList.setTitleFetchDetailsDtos(titleFetchDetailsDtos);
        uaeIbanTitleFetchDto.setUaeIbanTitleFetchDtoList(uaeIbanTitleFetchDtoList);
        uaeIbanTitleFetchResponse.setUaeIbanTitleFetch(uaeIbanTitleFetchDto);
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }

    @Test public void testAccountGetTitleUaeTitleFetchDto() {
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        UaeIbanTitleFetchDto uaeIbanTitleFetchDto = new UaeIbanTitleFetchDto();
        UaeIbanTitleFetchDtoList uaeIbanTitleFetchDtoList = new UaeIbanTitleFetchDtoList();
        List<TitleFetchDetailsDto> titleFetchDetailsDtos = new ArrayList<>();
        TitleFetchDetailsDto titleFetchDetailsDto = new TitleFetchDetailsDto();
        TitleFetchDetails titleFetchDetails = new TitleFetchDetails();
        TitleFetchDto titleFetchDto = new TitleFetchDto();
        titleFetchDetails.setTitleFetchDto(titleFetchDto);
        titleFetchDetailsDto.setTitleFetchDetails(titleFetchDetails);
        titleFetchDetailsDtos.add(titleFetchDetailsDto);
        uaeIbanTitleFetchDtoList.setTitleFetchDetailsDtos(titleFetchDetailsDtos);
        uaeIbanTitleFetchDto.setUaeIbanTitleFetchDtoList(uaeIbanTitleFetchDtoList);
        uaeIbanTitleFetchResponse.setUaeIbanTitleFetch(uaeIbanTitleFetchDto);
        //when
        when(omwExternalConfigProperties.getServiceId()).thenReturn("EFTS00001");
        when(omwExternalClient.getAccountTitle(any(),anyString())).thenReturn(uaeIbanTitleFetchResponse);
        //then
        String response = uaeAccountTitleFetchService.fetchAccountTitle("AE100330000010410000108");
        assertEquals("", response);

    }
    private UaeIbanTitleFetchResponse buildUaeIbanTitleFetchResponse(){
        UaeIbanTitleFetchResponse uaeIbanTitleFetchResponse = new UaeIbanTitleFetchResponse();
        UaeIbanTitleFetchDto uaeIbanTitleFetchDto = new UaeIbanTitleFetchDto();
        UaeIbanTitleFetchDtoList uaeIbanTitleFetchDtoList = new UaeIbanTitleFetchDtoList();
        List<TitleFetchDetailsDto> titleFetchDetailsDtos = new ArrayList<>();
        TitleFetchDetailsDto titleFetchDetailsDto = new TitleFetchDetailsDto();
        TitleFetchDetails titleFetchDetails = new TitleFetchDetails();
        TitleFetchDto titleFetchDto = new TitleFetchDto();
        titleFetchDto.setTitle("ACCOUNT NUSRDN");
        titleFetchDetails.setTitleFetchDto(titleFetchDto);
        titleFetchDetailsDto.setTitleFetchDetails(titleFetchDetails);
        titleFetchDetailsDtos.add(titleFetchDetailsDto);
        uaeIbanTitleFetchDtoList.setTitleFetchDetailsDtos(titleFetchDetailsDtos);
        uaeIbanTitleFetchDto.setUaeIbanTitleFetchDtoList(uaeIbanTitleFetchDtoList);
        uaeIbanTitleFetchResponse.setUaeIbanTitleFetch(uaeIbanTitleFetchDto);
        return uaeIbanTitleFetchResponse;
    }

}
