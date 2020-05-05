package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.AddressTypeDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerPhones;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferContext.Constants;
import com.mashreq.transfercoreservice.fundtransfer.mapper.QuickRemitInstaRemRequestMapper;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author shahbazkh
 * @date 5/5/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickRemitInstaRemStrategy implements QuickRemitFundTransfer {

    private final QuickRemitInstaRemRequestMapper mapper;
    private final QuickRemitFundTransferMWService quickRemitFundTransferMWService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO, ValidationContext context) {

        log.info("Quick Remit InstaRem initiated ");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setFinalName("");
        beneficiaryDto.setAddressLine1("");
        beneficiaryDto.setAddressLine2("");
        beneficiaryDto.setAddressLine3("");
        beneficiaryDto.setAccountNumber("");
        beneficiaryDto.setBankName("");
        beneficiaryDto.setCity("");
        beneficiaryDto.setState("");
        beneficiaryDto.setPingCode("");
        beneficiaryDto.setBankAccountType("");
        beneficiaryDto.setBeneficiaryCurrency("GBP");
        beneficiaryDto.setBeneficiaryRelationship("");
        beneficiaryDto.setBankCountry("GP");


        AccountDetailsDTO accountDetails = AccountDetailsDTO.builder()
                .number("")
                .currency("")
                .customerName("")
                .build();

        CustomerDetailsDto customerDetails = CustomerDetailsDto.builder()
                .address(Arrays.asList(AddressTypeDto.builder().addressType("R").address1("address 1").build()))
                .cifBranch("")
                .phones(Arrays.asList(CustomerPhones.builder().mobNumber("").phoneNumberType("P").build()))
                .uniqueIDName("")
                .uniqueIDName("")
                .build();

        BigDecimal transferAmountInSrcCurrency = new BigDecimal("10");
        BigDecimal exchangeRate = new BigDecimal("1");

        final FundTransferContext fundTransferContext = new FundTransferContext();
        fundTransferContext.add(Constants.BENEFICIARY_FUND_CONTEXT_KEY, beneficiaryDto);
        fundTransferContext.add(Constants.ACCOUNT_DETAILS_FUND_CONTEXT_KEY, accountDetails);
        fundTransferContext.add(Constants.CUSTOMER_DETAIL_FUND_CONTEXT_KEY, customerDetails);
        fundTransferContext.add(Constants.EXCHANGE_RATE_FUND_CONTEXT_KEY, exchangeRate);
        fundTransferContext.add(Constants.TRANSFER_AMOUNT_IN_SRC_CURRENCY_FUND_CONTEXT_KEY, transferAmountInSrcCurrency);

        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = mapper.mapTo(metadata, request, fundTransferContext);

        quickRemitFundTransferMWService.transfer(quickRemitFundTransferRequest);

        return null;
    }
}