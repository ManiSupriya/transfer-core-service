package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.AddressTypeDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerPhones;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;

import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;


import static com.mashreq.transfercoreservice.middleware.SoapServiceProperties.*;

@RunWith(MockitoJUnitRunner.class)
public class QuickRemitIndiaRequestMapperTest {

    @InjectMocks
    private QuickRemitIndiaRequestMapper quickRemitIndiaRequestMapper;

    @Mock
    private SoapServiceProperties soapServiceProperties;

    @Test
    public void test_mapping_for_india_when_primary_address_phone_present() {
        //given
        String bankName = "CITI BANK MGROAD";
        String bankCountry = "INDIA";
        String channelTraceId = "channelTraceId";
        String fullName = "Keshav kumar";
        BigDecimal txnAmt = new BigDecimal(1000);
        String beneficiaryCountryISO = "IN";
        String senderAcctNum = "1234567";
        String senderName = "Deepa S";
        String senderCurrency = "AED";
        String serviceCode = "TRTPTIN";
        String beneIFSC = "CITI0000004";
        String beneCurrency = "INR";
        String popCode =  "FAM";
        String popText =  "Family Support";
        String originatingCountry = "AE";
        String senderBankBranch = "Riqa Branch";
        String beneMobileNum = "1234567890";
        String srcIsoCurrency = "784";
        String destIsoCurrency = "356";
        AccountDetailsDTO accountDetails = AccountDetailsDTO.builder()
                .number(senderAcctNum)
                .customerName(senderName)
                .currency(senderCurrency)
                .availableBalance(new BigDecimal(3000))
                .branchCode("083")
                .accountName(senderName)
                .build();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setAmount(txnAmt);
        requestDTO.setPurposeCode(popCode);
        requestDTO.setPurposeDesc(popText);
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("111111111");
        beneficiaryDto.setFullName(fullName);
        beneficiaryDto.setFinalName(fullName);
        beneficiaryDto.setRoutingCode(beneIFSC);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setBankCountry(bankCountry);
        beneficiaryDto.setBeneficiaryCountryISO(beneficiaryCountryISO);
        beneficiaryDto.setBeneficiaryCurrency(beneCurrency);
        beneficiaryDto.setMobileNumber(beneMobileNum);

        BigDecimal transferAmountInSrcCurrency = new BigDecimal(50);
        BigDecimal exchangeRate = new BigDecimal(3.675);

        ValidationContext validationContext = new ValidationContext();
        validationContext.add("src-currency-iso", srcIsoCurrency);
        validationContext.add("src-country-iso", originatingCountry);

        CustomerDetailsDto customerDetails = CustomerDetailsDto.builder()
                .cifBranch(senderBankBranch)
                .nationality("IN")
                .phones(Arrays.asList(CustomerPhones.builder().mobNumber("1234567899").phoneNumberType("P").build(),
                        CustomerPhones.builder().mobNumber("1234567898").phoneNumberType("O").build()))
                .uniqueIDName("PASSPORT")
                .uniqueIDValue("33333333")
                .address(Arrays.asList(AddressTypeDto.builder()
                                .addressType("P")
                                .address1("Paddress1")
                                .address2("Paddress2")
                                .address3("Paddress3")
                                .build(),
                        AddressTypeDto.builder()
                                .addressType("R")
                                .address1("Raddress1")
                                .address2("Raddress2")
                                .address3("Raddress3")
                                .build())).build();
        //when
        final ServiceCodes serviceCodes = new ServiceCodes();
        serviceCodes.setQuickRemitIndia(serviceCode);
        Mockito.when(soapServiceProperties.getServiceCodes()).thenReturn(serviceCodes);

        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = quickRemitIndiaRequestMapper.map(channelTraceId, requestDTO, accountDetails, beneficiaryDto, transferAmountInSrcCurrency, exchangeRate, validationContext, customerDetails);

        //then
        Assert.assertEquals(serviceCode, quickRemitFundTransferRequest.getServiceCode());
        Assert.assertEquals(txnAmt, quickRemitFundTransferRequest.getAmountDESTCurrency());
        Assert.assertEquals(transferAmountInSrcCurrency, quickRemitFundTransferRequest.getAmountSRCCurrency());
        Assert.assertEquals(beneficiaryDto.getAccountNumber(), quickRemitFundTransferRequest.getBeneficiaryAccountNo());
        Assert.assertEquals("  ", quickRemitFundTransferRequest.getBeneficiaryAddress());
        Assert.assertEquals(beneIFSC, quickRemitFundTransferRequest.getBeneficiaryBankIFSC());
        Assert.assertEquals(bankName, quickRemitFundTransferRequest.getBeneficiaryBankName());
        Assert.assertEquals(bankCountry, quickRemitFundTransferRequest.getBeneficiaryCountry());
        Assert.assertEquals(fullName, quickRemitFundTransferRequest.getBeneficiaryFullName());
        Assert.assertEquals(fullName, quickRemitFundTransferRequest.getBeneficiaryName());
        Assert.assertEquals(channelTraceId, quickRemitFundTransferRequest.getChannelTraceId());
        Assert.assertEquals(beneficiaryCountryISO, quickRemitFundTransferRequest.getDestCountry());
        Assert.assertEquals(beneCurrency, quickRemitFundTransferRequest.getDestCurrency());
        Assert.assertEquals(srcIsoCurrency, quickRemitFundTransferRequest.getSrcISOCurrency());
        Assert.assertEquals(destIsoCurrency, quickRemitFundTransferRequest.getDestISOCurrency());
        Assert.assertEquals(exchangeRate, quickRemitFundTransferRequest.getExchangeRate());
        Assert.assertEquals(channelTraceId, quickRemitFundTransferRequest.getFinTxnNo());
        Assert.assertEquals(originatingCountry, quickRemitFundTransferRequest.getOriginatingCountry());
        Assert.assertEquals(popCode, quickRemitFundTransferRequest.getReasonCode());
        Assert.assertEquals(popText, quickRemitFundTransferRequest.getReasonText());
        Assert.assertEquals(senderAcctNum, quickRemitFundTransferRequest.getSenderBankAccount());
        Assert.assertEquals("IN", quickRemitFundTransferRequest.getSenderCountryISOCode());
        Assert.assertEquals(senderBankBranch, quickRemitFundTransferRequest.getSenderBankBranch());
        Assert.assertEquals("1234567899", quickRemitFundTransferRequest.getSenderMobileNo());
        Assert.assertEquals(senderName, quickRemitFundTransferRequest.getSenderName());
        Assert.assertEquals(senderCurrency, quickRemitFundTransferRequest.getSrcCurrency());
        Assert.assertEquals(txnAmt.toString(), quickRemitFundTransferRequest.getTransactionAmount());
        Assert.assertEquals(beneCurrency, quickRemitFundTransferRequest.getTransactionCurrency());
        Assert.assertEquals(beneMobileNum, quickRemitFundTransferRequest.getBeneficiaryMobileNo());
        Assert.assertEquals("Paddress1 Paddress2 Paddress3  ", quickRemitFundTransferRequest.getSenderAddress());
        Assert.assertEquals("33333333", quickRemitFundTransferRequest.getSenderIDNumber());
        Assert.assertEquals("PASSPORT", quickRemitFundTransferRequest.getSenderIDType());
    }

    @Test
    public void test_mapping_for_india_when_primary_address_phone_absent() {
        //given
        String bankName = "CITI BANK MGROAD";
        String bankCountry = "INDIA";
        String channelTraceId = "channelTraceId";
        String fullName = "Keshav kumar";
        BigDecimal txnAmt = new BigDecimal(1000);
        String beneficiaryCountryISO = "IN";
        String senderAcctNum = "1234567";
        String senderName = "Deepa S";
        String senderCurrency = "AED";
        String serviceCode = "TRTPTIN";
        String beneIFSC = "CITI0000004";
        String beneCurrency = "INR";
        String popCode =  "FAM";
        String popText =  "Family Support";
        String originatingCountry = "AE";
        String senderBankBranch = "Riqa Branch";
        String beneMobileNum = "1234567890";
        String srcIsoCurrency = "784";
        String destIsoCurrency = "356";
        AccountDetailsDTO accountDetails = AccountDetailsDTO.builder()
                .number(senderAcctNum)
                .customerName(senderName)
                .currency(senderCurrency)
                .availableBalance(new BigDecimal(3000))
                .branchCode("083")
                .accountName(senderName)
                .build();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setAmount(txnAmt);
        requestDTO.setPurposeCode(popCode);
        requestDTO.setPurposeDesc(popText);
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("111111111");
        beneficiaryDto.setFullName(fullName);
        beneficiaryDto.setFinalName(fullName);
        beneficiaryDto.setRoutingCode(beneIFSC);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setBankCountry(bankCountry);
        beneficiaryDto.setBeneficiaryCountryISO(beneficiaryCountryISO);
        beneficiaryDto.setBeneficiaryCurrency(beneCurrency);
        beneficiaryDto.setMobileNumber(beneMobileNum);

        BigDecimal transferAmountInSrcCurrency = new BigDecimal(50);
        BigDecimal exchangeRate = new BigDecimal(3.675);

        ValidationContext validationContext = new ValidationContext();
        validationContext.add("src-currency-iso", srcIsoCurrency);
        validationContext.add("src-country-iso", originatingCountry);

        CustomerDetailsDto customerDetails = CustomerDetailsDto.builder()
                .cifBranch(senderBankBranch)
                .nationality("IN")
                .phones(Arrays.asList(
                        CustomerPhones.builder().mobNumber("1234567898").phoneNumberType("O").build()))
                .uniqueIDName("PASSPORT")
                .uniqueIDValue("33333333")
                .address(Arrays.asList(AddressTypeDto.builder()
                                .addressType("O")
                                .address1("Oaddress1")
                                .address2("Oaddress2")
                                .address3("Oaddress3")
                                .build(),
                        AddressTypeDto.builder()
                                .addressType("R")
                                .address1("Raddress1")
                                .address2("Raddress2")
                                .address3("Raddress3")
                                .build())).build();
        //when
        final ServiceCodes serviceCodes = new ServiceCodes();
        serviceCodes.setQuickRemitIndia(serviceCode);
        Mockito.when(soapServiceProperties.getServiceCodes()).thenReturn(serviceCodes);

        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = quickRemitIndiaRequestMapper.map(channelTraceId, requestDTO, accountDetails, beneficiaryDto, transferAmountInSrcCurrency, exchangeRate, validationContext, customerDetails);

        //then
        Assert.assertEquals(serviceCode, quickRemitFundTransferRequest.getServiceCode());
        Assert.assertEquals(txnAmt, quickRemitFundTransferRequest.getAmountDESTCurrency());
        Assert.assertEquals(transferAmountInSrcCurrency, quickRemitFundTransferRequest.getAmountSRCCurrency());
        Assert.assertEquals(beneficiaryDto.getAccountNumber(), quickRemitFundTransferRequest.getBeneficiaryAccountNo());
        Assert.assertEquals("  ", quickRemitFundTransferRequest.getBeneficiaryAddress());
        Assert.assertEquals(beneIFSC, quickRemitFundTransferRequest.getBeneficiaryBankIFSC());
        Assert.assertEquals(bankName, quickRemitFundTransferRequest.getBeneficiaryBankName());
        Assert.assertEquals(bankCountry, quickRemitFundTransferRequest.getBeneficiaryCountry());
        Assert.assertEquals(fullName, quickRemitFundTransferRequest.getBeneficiaryFullName());
        Assert.assertEquals(fullName, quickRemitFundTransferRequest.getBeneficiaryName());
        Assert.assertEquals(channelTraceId, quickRemitFundTransferRequest.getChannelTraceId());
        Assert.assertEquals(beneficiaryCountryISO, quickRemitFundTransferRequest.getDestCountry());
        Assert.assertEquals(beneCurrency, quickRemitFundTransferRequest.getDestCurrency());
        Assert.assertEquals(srcIsoCurrency, quickRemitFundTransferRequest.getSrcISOCurrency());
        Assert.assertEquals(destIsoCurrency, quickRemitFundTransferRequest.getDestISOCurrency());
        Assert.assertEquals(exchangeRate, quickRemitFundTransferRequest.getExchangeRate());
        Assert.assertEquals(channelTraceId, quickRemitFundTransferRequest.getFinTxnNo());
        Assert.assertEquals(originatingCountry, quickRemitFundTransferRequest.getOriginatingCountry());
        Assert.assertEquals(popCode, quickRemitFundTransferRequest.getReasonCode());
        Assert.assertEquals(popText, quickRemitFundTransferRequest.getReasonText());
        Assert.assertEquals(senderAcctNum, quickRemitFundTransferRequest.getSenderBankAccount());
        Assert.assertEquals("IN", quickRemitFundTransferRequest.getSenderCountryISOCode());
        Assert.assertEquals(senderBankBranch, quickRemitFundTransferRequest.getSenderBankBranch());
        Assert.assertEquals("1234567898", quickRemitFundTransferRequest.getSenderMobileNo());
        Assert.assertEquals(senderName, quickRemitFundTransferRequest.getSenderName());
        Assert.assertEquals(senderCurrency, quickRemitFundTransferRequest.getSrcCurrency());
        Assert.assertEquals(txnAmt.toString(), quickRemitFundTransferRequest.getTransactionAmount());
        Assert.assertEquals(beneCurrency, quickRemitFundTransferRequest.getTransactionCurrency());
        Assert.assertEquals(beneMobileNum, quickRemitFundTransferRequest.getBeneficiaryMobileNo());
        Assert.assertEquals("Raddress1 Raddress2 Raddress3  ", quickRemitFundTransferRequest.getSenderAddress());
        Assert.assertEquals("33333333", quickRemitFundTransferRequest.getSenderIDNumber());
        Assert.assertEquals("PASSPORT", quickRemitFundTransferRequest.getSenderIDType());
    }


}