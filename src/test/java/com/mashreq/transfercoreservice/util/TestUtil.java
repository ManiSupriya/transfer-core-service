package com.mashreq.transfercoreservice.util;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.model.DigitalUserGroup;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.DeviceInfo;
import com.mashreq.transfercoreservice.notification.model.NotificationResponse;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.TransferType;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import java.math.BigDecimal;
import java.util.*;

public class TestUtil {

    public static CountryDto getCountryMs(){
        CountryDto countryMasterDto = new CountryDto();
        countryMasterDto.setCode("IN");
        countryMasterDto.setQuickRemitEnabled(true);
        return countryMasterDto;
    }

    public  static Map<String, CountryDto> getCountryMap(){
        Map<String, CountryDto> countryMap = new HashMap<>();
        countryMap.put("IN", getCountryMs());
        return countryMap;
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

    public static List<AccountDetailsDTO> getOwnAccountDetails(String fromAcc, String toAcc, String fromCurrency, String toCurrency) {
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setNumber(fromAcc);
        accountDetailsDTO.setCurrency(fromCurrency);

        AccountDetailsDTO accountDetailsDTO1 = new AccountDetailsDTO();
        accountDetailsDTO1.setNumber(toAcc);
        accountDetailsDTO1.setCurrency(toCurrency);

        return Arrays.asList(accountDetailsDTO, accountDetailsDTO1);
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
    public static BeneficiaryDto getPKCompanyBeneficiaryDto() {
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setServiceTypeCode("INFT");
        beneficiaryDto.setSwiftCode("CITILKLXXXX");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        beneficiaryDto.setBankCountryISO("PK");
        beneficiaryDto.setId(new Long(121));
        beneficiaryDto.setRoutingCode("X110280");
        beneficiaryDto.setBeneficiaryAccountType(BeneficiaryAccountType.COMPANY.name());
        return beneficiaryDto;
    }
    public static BeneficiaryDto getEGBeneficiaryDto() {
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setServiceTypeCode("INFT");
        beneficiaryDto.setSwiftCode("CITILKLXXXX");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.name());
        beneficiaryDto.setBankCountryISO("EG");
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

    public static LimitValidatorResponse limitValidatorResultsDto(String txnRefNo){
        LimitValidatorResponse limitValidatorResultsDto = new LimitValidatorResponse();
        limitValidatorResultsDto.setIsValid(true);
        limitValidatorResultsDto.setTransactionRefNo(txnRefNo);
        limitValidatorResultsDto.setVerificationType("ELIGIBLE");
        return limitValidatorResultsDto;
    }

    public static LimitValidatorResponse limitValidatorResultsDtoEligible(){
        LimitValidatorResponse limitValidatorResultsDto = new LimitValidatorResponse();
        limitValidatorResultsDto.setIsValid(true);
        limitValidatorResultsDto.setTransactionRefNo("1234");
        limitValidatorResultsDto.setMaxAmountMonthly("1500000");
        limitValidatorResultsDto.setMonthlyUsedAmount("252003");
        limitValidatorResultsDto.setMaxCountMonthly("1500");
        limitValidatorResultsDto.setMonthlyUsedCount("3");
        limitValidatorResultsDto.setMaxAmountDaily("200000");
        limitValidatorResultsDto.setDailyUsedAmount("0");
        limitValidatorResultsDto.setMaxCountDaily("300");
        limitValidatorResultsDto.setDailyUsedCount("0");
        limitValidatorResultsDto.setCoolingLimitAmount("20000");
        limitValidatorResultsDto.setCoolingLimitCount("2");
        limitValidatorResultsDto.setCoolingLimitAmount("0");
        limitValidatorResultsDto.setCoolingLimitCount("0");
        limitValidatorResultsDto.setMaxTrxAmount("200000");
        limitValidatorResultsDto.setAmountRemark(null);
        limitValidatorResultsDto.setCountRemark(null);
        limitValidatorResultsDto.setCurrentAvailableAmount(BigDecimal.valueOf(200000));
        limitValidatorResultsDto.setCoolingLimitCount("300");
        limitValidatorResultsDto.setAllowedToChangeLimit(true);
        limitValidatorResultsDto.setThreshold(BigDecimal.valueOf(500000));
        limitValidatorResultsDto.setMinimumLimitAmount(1000);
        limitValidatorResultsDto.setMaxMonthlyLimitChangeCount(1);
        limitValidatorResultsDto.setUsedMonthlyLimitChangeCount(0);
        limitValidatorResultsDto.setLimitChangeWindow("24");
        limitValidatorResultsDto.setSegmentLimit(BigDecimal.valueOf(200000));
        limitValidatorResultsDto.setLimitVersionUuid("C84495");
        limitValidatorResultsDto.setTransactionRefNo("07022445416");
        limitValidatorResultsDto.setErrorCode(null);
        limitValidatorResultsDto.setLastLimitChangeDate("Fri Feb 02 15:36:26 GST 2024");
        limitValidatorResultsDto.setNextLimitChangeDate(null);
        limitValidatorResultsDto.setVerificationType("ELIGIBLE");
        return limitValidatorResultsDto;
    }

    public static LimitValidatorResponse limitValidatorResultsDtoWithErrorCodes(String txnRefNo){
        LimitValidatorResponse limitValidatorResultsDto = new LimitValidatorResponse();
        limitValidatorResultsDto.setIsValid(true);
        limitValidatorResultsDto.setTransactionRefNo(txnRefNo);
        limitValidatorResultsDto.setVerificationType("LIMIT_INCREASE_ELIGIBLE");
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

    public static TransactionChargesDto getBankCharges() {
        TransactionChargesDto transactionChargesDto = new TransactionChargesDto();
        transactionChargesDto.setCoreBankTransactionFee(0.0);
        transactionChargesDto.setLocalTransactionCharge(0.0);
        transactionChargesDto.setInternationalTransactionalCharge(0.0);
        transactionChargesDto.setAccountClass("SAVACR");
        return transactionChargesDto;
    }

    public static QRDealDetails getQRDealsDetails() {
        QRDealDetails qrDealDetails = new QRDealDetails();
        qrDealDetails.setTotalLimitAmount(BigDecimal.ONE);
        return qrDealDetails;
    }

    public static Map<String, List<String>> getAccountContext() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("account-numbers",Arrays.asList("0123456789"));
        return map;
    }

    public static Map<String, List<String>> getMoneyTransferAccountContext() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("investment-account-number",Arrays.asList("1123456789"));
        map.put("account-numbers",Arrays.asList("0123456789"));
        return map;
    }
    
    public static Map<String, List<String>> getCardsContext() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("card-numbers",Arrays.asList("4444333322221111"));
        return map;
    }

    public static FundTransferResponse fundTransferResponse(String txnRefNo, MwResponseStatus status){
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(status);

        return FundTransferResponse.builder()
                .transactionRefNo(txnRefNo)
                .responseDto(coreFundTransferResponseDto)
                .build();
    }

    public static CifProductsDto getAccountDetailsDTOS() {
        CifProductsDto cifProductsDto = new CifProductsDto();
        SearchAccountDto fromAcc1 = new SearchAccountDto();
        fromAcc1.setNumber("010797697124");
        fromAcc1.setAccountType(new SearchAccountTypeDto());
        fromAcc1.getAccountType().setSchemaType("test2");
        SearchAccountDto fromAcc2 = new SearchAccountDto();
        fromAcc2.setNumber("019010050532");
        fromAcc2.setAccountType(new SearchAccountTypeDto());
        fromAcc2.getAccountType().setSchemaType("test1");
        SearchAccountDto fromAcc3 = new SearchAccountDto();
        fromAcc3.setNumber("019010073901");
        fromAcc3.setAccountType(new SearchAccountTypeDto());
        fromAcc3.getAccountType().setSchemaType("test");
        cifProductsDto.setAccounts(Arrays.asList(fromAcc1, fromAcc2, fromAcc3));

        return cifProductsDto;
    }

    public static TransferLimitRequestDto buildTransferLimitRequest() {
        return TransferLimitRequestDto.builder()
                .beneficiaryId(123L)
                .amount(new BigDecimal(599))
                .orderType(FTOrderType.SI)
                .transferType(TransferType.QR)
                .accountCurrency("AED")
                .build();
    }
    public static CurrencyConversionDto buildCurrencyConversionDto() {
    	CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
    	currencyConversionDto.setTransactionAmount(new BigDecimal(45));
        return currencyConversionDto;
    }

    public static CustomerNotification getCustomerNotification(){
        Segment segment = new Segment();
        segment.setName("CONV");
        CustomerNotification customerNotification = new CustomerNotification();
        customerNotification.setCustomerName("TestUSer");
        customerNotification.setAmount("23");
        customerNotification.setChannel("MOBILE");
        customerNotification.setCreditAccount("01900235092");
        customerNotification.setBeneficiaryName("TEST BENE USER");
        customerNotification.setSegment(segment);
        return  customerNotification;
    }

    public static RequestMetaData getRequestMetadata(){
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setMobileNUmber("0588678123");
        requestMetaData.setPrimaryCif("01245553");
        requestMetaData.setCountry("AE");
        return requestMetaData;
    }

    public static UserDTO getUserDto(){
        UserDTO userDTO = new UserDTO();
        userDTO.setDeviceInfo(String.valueOf(DeviceInfo.ANDROID));
        userDTO.setDeviceRegisteredForPush("POWDWFECWSCVSA323fq");
        return userDTO;
    }
    public static NotificationResponse getNotificationResponse(){
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setTrackingId("224444");
        notificationResponse.setNotificationId("NOTIFY9282");
        return notificationResponse;
    }
}
