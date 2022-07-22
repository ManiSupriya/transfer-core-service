package com.mashreq.transfercoreservice.banksearch;


import com.mashreq.transfercoreservice.common.LocalIbanValidator;
import com.mashreq.transfercoreservice.dto.BankResolverRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.repository.BankRepository;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class AccountBasedBankDetailsResolverTest {

    @InjectMocks
    private AccountBasedBankDetailsResolver accountBasedBankDetailsResolver;
    @Mock
    private IbanResolver ibanResolver;
    @Mock
    private LocalIbanValidator localIbanValidator;
    @Mock
    private BankRepository bankRepository;

    @Test
    public void test_account_bank_search_for_local_non_mashreq() {
        //Given

        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("00029991234567")
                .branchCode("0001")
                .bankCode("0036")
                .journeyType("MT")
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
        Mockito.when(ibanResolver.constructIBAN(Mockito.eq("00029991234567"), Mockito.eq("0036"),Mockito.eq("0001"))).thenReturn("EG450036000100000029991234567");
        Mockito.when(localIbanValidator.isLocalIban(Mockito.any())).thenReturn(true);
        Mockito.when(localIbanValidator.validate(Mockito.any())).thenReturn("0036");
        Mockito.when(bankRepository.findByBankCode(Mockito.eq("0036"))).thenReturn(Optional.of(list));
        Mockito.when(localIbanValidator.extractAccountNumberIfMashreqIban(Mockito.anyString(),Mockito.anyString())).thenReturn(null);
        List<BankResultsDto> result = accountBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto);

        //then
        Assertions.assertEquals("EG450036000100000029991234567", result.get(0).getIbanNumber());
        Assertions.assertEquals("CREDEGCAXXX", result.get(0).getSwiftCode());
        Assertions.assertEquals("Credit Agricole", result.get(0).getBankName());
        Assertions.assertEquals("Main Branch", result.get(0).getBranchName());
    }


    @Test
    public void test_account_bank_search_for_local_mashreq() {
        //Given

        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("00059010006621")
                .branchCode("0004")
                .bankCode("0046")
                .journeyType("MT")
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
        Mockito.when(ibanResolver.constructIBAN(Mockito.eq("00059010006621"), Mockito.eq("0046"),Mockito.eq("0004"))).thenReturn("EG400046000400000059010006621");
        Mockito.when(localIbanValidator.isLocalIban(Mockito.any())).thenReturn(true);
        Mockito.when(localIbanValidator.validate(Mockito.any())).thenReturn("0046");
        Mockito.when(bankRepository.findByBankCode(Mockito.eq("0046"))).thenReturn(Optional.of(list));
        Mockito.when(localIbanValidator.extractAccountNumberIfMashreqIban(Mockito.anyString(),Mockito.anyString())).thenReturn("00000059010006621");
        List<BankResultsDto> result = accountBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto);

        //then
        Assertions.assertNull( result.get(0).getIbanNumber());
        Assertions.assertEquals("MSHQEGCAXXX", result.get(0).getSwiftCode());
        Assertions.assertEquals("Mashreq Bank", result.get(0).getBankName());
        Assertions.assertEquals("Main Branch", result.get(0).getBranchName());
        Assertions.assertEquals("00000059010006621", result.get(0).getAccountNo());
    }



}
