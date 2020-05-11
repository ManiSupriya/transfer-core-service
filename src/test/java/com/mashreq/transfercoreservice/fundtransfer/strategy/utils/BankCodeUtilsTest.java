package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.*;

public class BankCodeUtilsTest {

    @Test
    public void should_return_swift_based_routing_code() {

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setSwiftCode("CITIBDBDXXX");
        RoutingCode routingCode = BankCodeUtils.extractBankCode(beneficiaryDto);
        assertThat(routingCode, CoreMatchers.is(new RoutingCode("SWIFT", "CITIBDBDXXX")));

    }

//    @Test
//    public void should_return_routing_code() {
//
//        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
//        beneficiaryDto.setRoutingCode("062001");
//        beneficiaryDto.setBankCountry("AU");
//        RoutingCode routingCode = BankCodeUtils.extractBankCode(beneficiaryDto);
//        assertThat(routingCode, CoreMatchers.is(new RoutingCode("BSB CODE", "062001")));
//
//    }
}