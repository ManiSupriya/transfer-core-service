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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class QuickRemitPakistanRequestMapperTest {

    @InjectMocks
    private QuickRemitPakistanRequestMapper quickRemitPakistanRequestMapper;

    @Mock
    private SoapServiceProperties soapServiceProperties;

    @Test
    public void test_() {
        //given
        String bankName = "FAYSAL BANK JINNA ROAD";
        String bankCountry = "PAKISTAN";
        String channelTraceId = "channelTraceId";
        String fullName = "Keshav kumar";
        BigDecimal txnAmt = new BigDecimal(1000);
        String beneficiaryCountryISO = "PK";
        String senderAcctNum = "1234567";
        String senderName = "Deepa S";
        String senderCurrency = "AED";
        String serviceCode = "TRTPTPK";
        String beneCurrency = "PKR";
        String popCode =  "FAM";
        String popText =  "Family Support";
        String originatingCountry = "AE";
        String senderBankBranch = "Riqa Branch";
        String beneMobileNum = "1234567890";
        String srcIsoCurrency = "784";
        String destIsoCurrency = "586";
        String beneBankCode = "076";
        String docType= "PASSPORT_CNIC";
        String docNum= "23452345";
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
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setBankCountry(bankCountry);
        beneficiaryDto.setBeneficiaryCountryISO(beneficiaryCountryISO);
        beneficiaryDto.setBeneficiaryCurrency(beneCurrency);
        beneficiaryDto.setMobileNumber(beneMobileNum);
        beneficiaryDto.setDocumentType(docType);
        beneficiaryDto.setDocumentNumber(docNum);
        beneficiaryDto.setBankCode(beneBankCode);

        BigDecimal transferAmountInSrcCurrency = new BigDecimal(50);
        BigDecimal exchangeRate = new BigDecimal(3.675);

        ValidationContext validationContext = new ValidationContext();
        validationContext.add("src-currency-iso", srcIsoCurrency);
        validationContext.add("src-country-iso", originatingCountry);
        List<CustomerPhones> customerPhonesList = new ArrayList<>();
        CustomerPhones customerPhones = new CustomerPhones();
        customerPhones.setMobNumber("1234567899");
        customerPhones.setPhoneNumberType("P");
        CustomerPhones customerPhones1 = new CustomerPhones();
        customerPhones1.setMobNumber("1234567898");
        customerPhones1.setPhoneNumberType("O");
        customerPhonesList.add(customerPhones1);
        customerPhonesList.add(customerPhones);
        List<AddressTypeDto> addressTypeDtoList = new ArrayList<>();
        AddressTypeDto addressTypeDto = new AddressTypeDto();
        addressTypeDto.setAddressType("P");
        addressTypeDto.setAddress1("Paddress1");
        addressTypeDto.setAddress2("Paddress2");
        addressTypeDto.setAddress3("Paddress3");
        AddressTypeDto addressTypeDto1 = new AddressTypeDto();
        addressTypeDto1.setAddressType("R");
        addressTypeDto1.setAddress1("Raddress1");
        addressTypeDto1.setAddress2("Raddress2");
        addressTypeDto1.setAddress3("Raddress3");
        addressTypeDtoList.add(addressTypeDto);
        addressTypeDtoList.add(addressTypeDto1);
        CustomerDetailsDto customerDetailsDto = new CustomerDetailsDto();
        customerDetailsDto.setCifBranch(senderBankBranch);
        customerDetailsDto.setNationality("UK");
        customerDetailsDto.setPhones(customerPhonesList);
        customerDetailsDto.setAddress(addressTypeDtoList);
        customerDetailsDto.setUniqueIDName("PASSPORT");
        customerDetailsDto.setUniqueIDValue("33333333");
        //when
        final SoapServiceProperties.ServiceCodes serviceCodes = new SoapServiceProperties.ServiceCodes();
        serviceCodes.setQuickRemitPakistan(serviceCode);
        Mockito.when(soapServiceProperties.getServiceCodes()).thenReturn(serviceCodes);

        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = quickRemitPakistanRequestMapper.map
                (channelTraceId, requestDTO, accountDetails, beneficiaryDto, transferAmountInSrcCurrency,
                        exchangeRate, validationContext, customerDetailsDto);

        //then
        Assert.assertEquals(serviceCode, quickRemitFundTransferRequest.getServiceCode());
        Assert.assertEquals(txnAmt, quickRemitFundTransferRequest.getAmountDESTCurrency());
        Assert.assertEquals(transferAmountInSrcCurrency, quickRemitFundTransferRequest.getAmountSRCCurrency());
        Assert.assertEquals(beneficiaryDto.getAccountNumber(), quickRemitFundTransferRequest.getBeneficiaryAccountNo());
        Assert.assertEquals("  ", quickRemitFundTransferRequest.getBeneficiaryAddress());
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
        Assert.assertEquals("UK", quickRemitFundTransferRequest.getSenderCountryISOCode());
        Assert.assertEquals(senderBankBranch, quickRemitFundTransferRequest.getSenderBankBranch());
        Assert.assertEquals("1234567899", quickRemitFundTransferRequest.getSenderMobileNo());
        Assert.assertEquals(senderName, quickRemitFundTransferRequest.getSenderName());
        Assert.assertEquals(senderCurrency, quickRemitFundTransferRequest.getSrcCurrency());
        Assert.assertEquals(txnAmt.toString(), quickRemitFundTransferRequest.getTransactionAmount());
        Assert.assertEquals(beneCurrency, quickRemitFundTransferRequest.getTransactionCurrency());
        Assert.assertEquals("Direct Credit", quickRemitFundTransferRequest.getDistributionType());
        Assert.assertEquals("FT", quickRemitFundTransferRequest.getTransferType());
        Assert.assertEquals(beneMobileNum, quickRemitFundTransferRequest.getBeneficiaryMobileNo());
        Assert.assertEquals(beneBankCode, quickRemitFundTransferRequest.getBeneficiaryBankCode());
        Assert.assertEquals(docType, quickRemitFundTransferRequest.getBeneficiaryIdType());
        Assert.assertEquals("Paddress1 Paddress2 Paddress3  ", quickRemitFundTransferRequest.getSenderAddress());
        Assert.assertEquals("33333333", quickRemitFundTransferRequest.getSenderIDNumber());
        Assert.assertEquals("PASSPORT", quickRemitFundTransferRequest.getSenderIDType());
        //Assert.assertEquals(docNum, quickRemitFundTransferRequest.getBeneficiaryIDNo());
    }







}
