package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

public class BankCodeExtractorTest {

    @Test
    public void should_return_swift_code() {

        InstaRemCountryRules instaRemCountryRules = new InstaRemCountryRules();
        instaRemCountryRules.setBankCodeType(getMockedMap());

        BankCodeExtractor bankCodeExtractor = new BankCodeExtractor(instaRemCountryRules);

        BeneficiaryDto mockBeneficiaryDto = new BeneficiaryDto();
        mockBeneficiaryDto.setBeneficiaryCountryISO("HU");
        mockBeneficiaryDto.setSwiftCode("HUFIGB21XXX");
        List<RoutingCode> routingCode = bankCodeExtractor.getRoutingCodes(mockBeneficiaryDto);


        assertThat(routingCode, notNullValue());
        assertThat(routingCode, hasSize(1));
        assertThat(routingCode.get(0).getType(), CoreMatchers.is("SWIFT"));
        assertThat(routingCode.get(0).getValue(), CoreMatchers.is("HUFIGB21XXX"));
    }


    @Test
    public void should_return_sort_code_and_swift_code() {

        InstaRemCountryRules instaRemCountryRules = new InstaRemCountryRules();
        instaRemCountryRules.setBankCodeType(getMockedMap());
        BankCodeExtractor bankCodeExtractor = new BankCodeExtractor(instaRemCountryRules);

        BeneficiaryDto mockBeneficiaryDto = new BeneficiaryDto();
        mockBeneficiaryDto.setBeneficiaryCountryISO("GB");
        mockBeneficiaryDto.setSwiftCode("HBUKGB4BXXX");
        mockBeneficiaryDto.setRoutingCode("407133");
        List<RoutingCode> routingCode = bankCodeExtractor.getRoutingCodes(mockBeneficiaryDto);


        assertThat(routingCode, notNullValue());
        assertThat(routingCode, hasSize(2));

        assertThat(routingCode.get(0).getType(), CoreMatchers.is("SORT CODE"));
        assertThat(routingCode.get(0).getValue(), CoreMatchers.is("407133"));

        assertThat(routingCode.get(1).getType(), CoreMatchers.is("SWIFT"));
        assertThat(routingCode.get(1).getValue(), CoreMatchers.is("HBUKGB4BXXX"));

    }

    @Test
    public void should_return_transit_code_and_swift_code() {

        InstaRemCountryRules instaRemCountryRules = new InstaRemCountryRules();
        instaRemCountryRules.setBankCodeType(getMockedMap());
        BankCodeExtractor bankCodeExtractor = new BankCodeExtractor(instaRemCountryRules);

        BeneficiaryDto mockBeneficiaryDto = new BeneficiaryDto();
        mockBeneficiaryDto.setBeneficiaryCountryISO("CA");
        mockBeneficiaryDto.setSwiftCode("ROYCCAT2XXX");
        mockBeneficiaryDto.setRoutingCode("000301001");
        List<RoutingCode> routingCode = bankCodeExtractor.getRoutingCodes(mockBeneficiaryDto);


        assertThat(routingCode, notNullValue());
        assertThat(routingCode, hasSize(2));

        assertThat(routingCode.get(0).getType(), CoreMatchers.is("TRANSIT NUMBER"));
        assertThat(routingCode.get(0).getValue(), CoreMatchers.is("1001"));

        assertThat(routingCode.get(1).getType(), CoreMatchers.is("SWIFT"));
        assertThat(routingCode.get(1).getValue(), CoreMatchers.is("ROYCCAT2XXX"));

    }

    private HashMap<String, Map<String,String>> getMockedMap() {
        HashMap<String, Map<String,String>> map = new HashMap<>();

        //India
        map.put("IN", new HashMap<String, String>() {{
            put("code", "IFSC CODE");
        }});

        //Great Britan
        map.put("GB", new HashMap<String, String>() {{
            put("code", "SORT CODE");
        }});

        //Canada
        map.put("CA", new HashMap<String, String>() {{
            put("code", "TRANSIT NUMBER");
            put("substr-index-before-assigning-to-routing-code", "5");
        }});

        //Australia
        map.put("AU", new HashMap<String, String>() {{
            put("AU", "BSB CODE");
        }});

        //Sri Lanka
        map.put("LK", new HashMap<String, String>() {{
            put("code", "BRANCH CODE");
        }});

        return map;
    }
}