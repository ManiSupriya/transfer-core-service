package com.mashreq.transfercoreservice.banksearch;


import com.mashreq.transfercoreservice.client.service.AccountService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class AccountBasedBankDetailsResolverTest {

    @InjectMocks
    private AccountBasedBankDetailsResolver accountBasedBankDetailsResolver;
    @Mock
    private BankRepository bankRepository;
    @Mock
    private AccountService accountService;

    @Test
    public void test_account_bank_search_for_local_non_mashreq() {
        //Given
        ReflectionTestUtils.setField(accountBasedBankDetailsResolver,"localBankCode","0046");
        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("00029991234567")
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
        Mockito.when(bankRepository.findByBankCode(Mockito.eq("0036"))).thenReturn(Optional.of(list));
        List<BankResultsDto> result = accountBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto);

        //then
        Assertions.assertEquals("00029991234567", result.get(0).getIbanNumber());
        Assertions.assertEquals("CREDEGCAXXX", result.get(0).getSwiftCode());
        Assertions.assertEquals("Credit Agricole", result.get(0).getBankName());

    }


    @Test
    public void test_account_bank_search_for_local_mashreq() {
        //Given
        ReflectionTestUtils.setField(accountBasedBankDetailsResolver,"localBankCode","0046");
        BankResolverRequestDto bankResolverRequestDto = BankResolverRequestDto.builder()
                .identifier("00059010006621")
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
        Mockito.when(accountService.isAccountBelongsToMashreq("00059010006621")).thenReturn(true);
        Mockito.when(bankRepository.findByBankCode(Mockito.eq("0046"))).thenReturn(Optional.of(list));
        BankResultsDto result = accountBasedBankDetailsResolver.getBankDetails(bankResolverRequestDto).get(0);

        //then

        Assertions.assertEquals("MSHQEGCAXXX", result.getSwiftCode());
        Assertions.assertEquals("Mashreq Bank", result.getBankName());
        Assertions.assertEquals("00059010006621", result.getAccountNo());
    }



}
