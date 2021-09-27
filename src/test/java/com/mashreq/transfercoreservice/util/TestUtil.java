package com.mashreq.transfercoreservice.util;

import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static CoreCardDetailsDto getCardDetails() {
        CoreCardDetailsDto coreCardDetailsDto = new CoreCardDetailsDto();
        coreCardDetailsDto.setAccountType("Savings");
        coreCardDetailsDto.setExpiryDate("2020-08-04");
        return coreCardDetailsDto;
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
        DigitalUser digitalUser = new DigitalUser();
        digitalUser = new DigitalUser();
        digitalUser.setDeviceInfo("ANDROID");
        return digitalUser;
    }

    public static LimitValidatorResultsDto limitValidatorResultsDto(){
        LimitValidatorResultsDto limitValidatorResultsDto = new LimitValidatorResultsDto();
        limitValidatorResultsDto.setValid(true);
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
