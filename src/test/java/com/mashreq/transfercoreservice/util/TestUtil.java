package com.mashreq.transfercoreservice.util;

import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.AdditionalFields;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.model.DigitalUserGroup;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import java.math.BigDecimal;
import java.util.*;

public class TestUtil {

    public static List<CountryMasterDto> getCountryMs(){
        CountryMasterDto countryMasterDto = new CountryMasterDto();
        countryMasterDto.setCode("IN");
        countryMasterDto.setQuickRemitEnabled(true);
        return Arrays.asList(countryMasterDto);
    }

    public static QRExchangeResponse qrExchangeResponse(){
        QRExchangeResponse response = new QRExchangeResponse();
        response.setAccountCurrency("USD");
        response.setTransactionCurrency("NPR");
        response.setExchangeRate("0.00872600");
        response.setDebitAmountWithoutCharges("43.63");
        response.setTransactionAmount("5000.00");
        response.setAllowQR(true);
        return response;
    }

    public static CoreCardDetailsDto getCardDetails() {
        CoreCardDetailsDto coreCardDetailsDto = new CoreCardDetailsDto();
        coreCardDetailsDto.setAccountType("Savings");
        coreCardDetailsDto.setExpiryDate("2020-08-04");
        return coreCardDetailsDto;
    }

    public static CoreAccountDetailsDTO getCoreAccountDetails() {
        CoreAccountDetailsDTO coreAccountDetailsDTO = new CoreAccountDetailsDTO();
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setNumber("1234567890");
        searchAccountDto.setAccountType(new SearchAccountTypeDto());
        coreAccountDetailsDTO.setConnectedAccounts(Arrays.asList(searchAccountDto));
        return coreAccountDetailsDTO;
    }

    public static AdditionalFields getAdditionalFields(){
        AdditionalFields additionalFields = new AdditionalFields();
        Map<String, String> map = new HashMap<>();
        map.put("fullName","test");
        additionalFields.setIncorrectFields(map);

        return additionalFields;
    }

    public static List<AccountDetailsDTO> getAccountDetails() {
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setNumber("1234567890");
        accountDetailsDTO.setCurrency("AED");
        return Arrays.asList(accountDetailsDTO);
    }

    public static CustomerDetailsDto getCustomerDetails() {
        CustomerDetailsDto customerDetailsDto = new CustomerDetailsDto();
        customerDetailsDto.setCustomerFullName("Full Name");
        return customerDetailsDto;
    }

    public static BeneficiaryDto getBeneficiaryDto() {
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setServiceTypeCode("INFT");
        beneficiaryDto.setSwiftCode("CITILKLXXXX");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        beneficiaryDto.setBankCountryISO("IN");
        beneficiaryDto.setId(new Long(121));
        beneficiaryDto.setRoutingCode("X110280");
        return beneficiaryDto;
    }

    public static BeneficiaryDto getBeneficiaryDtoV2() {
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setServiceTypeCode("INFT");
        beneficiaryDto.setSwiftCode("CITILKLXXXX");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        beneficiaryDto.setBankCountryISO("IN");
        beneficiaryDto.setId(new Long(121));
        beneficiaryDto.setRoutingCode("X110280");
        beneficiaryDto.setNewVersion(true);
        return beneficiaryDto;
    }

    public static CurrencyConversionDto getCurrencyConversionDto() {
        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setTransactionAmount(new BigDecimal(10));
        currencyConversionDto.setAccountCurrencyAmount(new BigDecimal(20));
        currencyConversionDto.setExchangeRate(new BigDecimal(1.0));
        return currencyConversionDto;
    }

    public static DigitalUser getDigitalUser() {
        DigitalUser digitalUser = null;
        digitalUser = new DigitalUser();
        digitalUser.setDeviceInfo("ANDROID");

        DigitalUserGroup digitalUserGroup = new DigitalUserGroup();
        Segment segment = new Segment();
        Country country = new Country();

        digitalUserGroup.setSegment(segment);
        digitalUserGroup.setCountry(country);

        digitalUser.setDigitalUserGroup(digitalUserGroup);

        return digitalUser;
    }

    public static LimitValidatorResponse limitValidatorResultsDto(){
        LimitValidatorResponse limitValidatorResultsDto = new LimitValidatorResponse();
        limitValidatorResultsDto.setIsValid(true);
        return limitValidatorResultsDto;
    }

    public static List<AddressTypeDto> getAddress(){
        AddressTypeDto addressTypeDto = new AddressTypeDto();
        addressTypeDto.setAddressType("P");
        addressTypeDto.setAddress1("add1");
        AddressTypeDto addressTypeDto2 = new AddressTypeDto();
        addressTypeDto2.setAddressType("C");
        addressTypeDto2.setAddress1("add1");
        addressTypeDto2.setAddress1("add2");
        List<AddressTypeDto> list = new ArrayList<>();
        list.add(addressTypeDto);
        list.add(addressTypeDto2);
        return list;

    }

    public static <T> Response<T> getSuccessResponse(T data){
        return Response.<T>builder().status(ResponseStatus.SUCCESS).data(data).build();
    }

    public static  Response getEmptyErrorResponse(){
        return Response.builder().status(ResponseStatus.ERROR).build();
    }

    public static <T> Response<T> getErrorResponse(T data){
        return Response.<T>builder().status(ResponseStatus.ERROR).data(data).build();
    }
}
