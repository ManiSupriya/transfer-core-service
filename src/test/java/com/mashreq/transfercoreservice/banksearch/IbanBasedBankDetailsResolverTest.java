package com.mashreq.transfercoreservice.banksearch;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.common.LocalIbanValidator;
import com.mashreq.transfercoreservice.config.feign.OmwExternalConfigProperties;
import com.mashreq.transfercoreservice.dto.BankResolverRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class IbanBasedBankDetailsResolverTest {

    @InjectMocks
    public IbanBasedBankDetailsResolver ibanBasedBankDetailsResolver;
    @Mock
    private LocalIbanValidator localIbanValidator;
    @Mock
    private IbanSearchMWService ibanSearchMWService;
    @Mock
    private BankRepository bankRepository;
    @Mock
    private UAEAccountTitleFetchService uaeAccountTitleFetchService;
    @Mock
    private OmwExternalConfigProperties omwExternalConfigProperties;

    @Test
    public void test_iban_search_for_local_non_mashreq() {
        //Given

        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("EG450036000100000029991234567")
                .journeyType("MT")
                .requestMetaData(new RequestMetaData())
                .build();
        BankDetails bankDetails1= new BankDetails();
        bankDetails1.setBankName("Credit Agricole");
        bankDetails1.setBankCode("0036");
        bankDetails1.setBranchName("Main Branch");
        bankDetails1.setBranchCode("0001");
        bankDetails1.setSwiftCode("CREDEGCAXXX");
        BankDetails bankDetails2= new BankDetails();
        bankDetails2.setBankName("Credit Agricole");
        bankDetails2.setBankCode("0036");
        bankDetails2.setBranchName("Main Branch 2");
        bankDetails2.setBranchCode("0002");
        bankDetails2.setSwiftCode("CREDEGCAXXX");
        List<BankDetails> list = Arrays.asList(bankDetails1, bankDetails2);
        //when
        Mockito.when(localIbanValidator.isLocalIban(Mockito.any())).thenReturn(true);
        Mockito.when(localIbanValidator.validate(Mockito.any())).thenReturn("0036");
        Mockito.when(bankRepository.findByBankCode(Mockito.eq("0036"))).thenReturn(Optional.of(list));
        Mockito.when(localIbanValidator.extractAccountNumberIfMashreqIban(anyString(), anyString())).thenReturn(null);
        List<BankResultsDto> result = ibanBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto);

        //then
        Assertions.assertEquals("EG450036000100000029991234567", result.get(0).getIbanNumber());
        Assertions.assertEquals("CREDEGCAXXX", result.get(0).getSwiftCode());
        Assertions.assertEquals("Credit Agricole", result.get(0).getBankName());
    }

    @Test
    public void test_iban_search_for_local_mashreq() {
        //Given

        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("EG400046000400000059010006621")
                .journeyType("MT")
                .requestMetaData(new RequestMetaData())
                .build();
        BankDetails bankDetails1= new BankDetails();
        bankDetails1.setBankName("Mashreq Bank");
        bankDetails1.setBankCode("0046");
        bankDetails1.setBranchName("Main Branch");
        bankDetails1.setBranchCode("0004");
        bankDetails1.setSwiftCode("MSHQEGCAXXX");
        BankDetails bankDetails2= new BankDetails();
        bankDetails2.setBankName("Mashreq Bank");
        bankDetails2.setBankCode("0046");
        bankDetails2.setBranchName("Main Branch 2");
        bankDetails2.setBranchCode("0006");
        bankDetails2.setSwiftCode("MSHQEGCAXXX");
        List<BankDetails> list = Arrays.asList(bankDetails1, bankDetails2);
        //when
        Mockito.when(localIbanValidator.isLocalIban(Mockito.any())).thenReturn(true);
        Mockito.when(localIbanValidator.validate(Mockito.any())).thenReturn("0046");
        Mockito.when(bankRepository.findByBankCode(Mockito.eq("0046"))).thenReturn(Optional.of(list));
        Mockito.when(localIbanValidator.extractAccountNumberIfMashreqIban(anyString(), anyString())).thenReturn("00000059010006621");
        List<BankResultsDto> result = ibanBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto);

        //then
        Assertions.assertEquals("EG400046000400000059010006621", result.get(0).getIbanNumber());
        Assertions.assertEquals("MSHQEGCAXXX", result.get(0).getSwiftCode());
        Assertions.assertEquals("Mashreq Bank", result.get(0).getBankName());
        Assertions.assertEquals("00000059010006621", result.get(0).getAccountNo());
    }

    @Test
    public void test_iban_search_from_uae_to_egypt_local_non_mashreq_iban() {
        //Given
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setChannelTraceId("trace123");
        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("EG450036000100000029991234567")
                .journeyType("MT")
                .requestMetaData(requestMetaData)
                .build();
        BankResultsDto bankResultsDto = new BankResultsDto();
        bankResultsDto.setIbanNumber("EG450036000100000029991234567");
        bankResultsDto.setBankName("Credit Agricole");
        bankResultsDto.setSwiftCode("CREDEGCAXXX");
        bankResultsDto.setBranchName("Main Branch");

        //when
        Mockito.when(localIbanValidator.isLocalIban(Mockito.any())).thenReturn(false);

        Mockito.when(ibanSearchMWService.fetchBankDetailsWithIban(Mockito.eq(requestMetaData.getChannelTraceId()),Mockito.eq("EG450036000100000029991234567"), Mockito.eq(requestMetaData))).thenReturn(Arrays.asList(bankResultsDto));
        List<BankResultsDto> result = ibanBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto);

        //then
        Assertions.assertEquals(bankResultsDto, result.get(0));

    }
    @Test
    public void test_iban_search_AE_local_non_mashreq() {
        //Given

        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("AE100330000010410000108")
                .journeyType("MT")
                .requestMetaData(new RequestMetaData())
                .build();
        BankDetails bankDetails1= new BankDetails();
        bankDetails1.setBankName("Credit Agricole");
        bankDetails1.setBankCode("0036");
        bankDetails1.setBranchName("Main Branch");
        bankDetails1.setBranchCode("0001");
        bankDetails1.setSwiftCode("CREDEGCAXXX");
        BankDetails bankDetails2= new BankDetails();
        bankDetails2.setBankName("Credit Agricole");
        bankDetails2.setBankCode("0036");
        bankDetails2.setBranchName("Main Branch 2");
        bankDetails2.setBranchCode("0002");
        bankDetails2.setSwiftCode("CREDEGCAXXX");
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
        List<BankDetails> list = Arrays.asList(bankDetails1, bankDetails2);
        //when
        Mockito.when(localIbanValidator.isLocalIban(anyString())).thenReturn(true);
        Mockito.when(localIbanValidator.validate(anyString())).thenReturn("0036");
        Mockito.when(bankRepository.findByBankCode(anyString())).thenReturn(Optional.of(list));
        Mockito.when(localIbanValidator.extractAccountNumberIfMashreqIban(anyString(), anyString())).thenReturn(null);
        Mockito.when(omwExternalConfigProperties.isTitleFetchEnabled()).thenReturn(true);
        Mockito.when(uaeAccountTitleFetchService.getAccountTitle(any())).thenReturn("ACCOUNT NUSRDN");
        List<BankResultsDto> result = ibanBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto);

        //then
        Assertions.assertEquals("AE100330000010410000108", result.get(0).getIbanNumber());
        Assertions.assertEquals("CREDEGCAXXX", result.get(0).getSwiftCode());
        Assertions.assertEquals("Credit Agricole", result.get(0).getBankName());
    }

}
