package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;


import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.AddressTypeDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author shahbazkh
 * @date 4/23/20
 */
public class CustomerDetailsUtils {

    private static final String COMMA = ",";
    private static final List<String> ADDRESS_TYPES = Arrays.asList("P", "R", "O");
    private static final List<String> PHONE_NUMBER_TYPES = Arrays.asList("P", "O");

    /**
     *
     * @param beneficiaryDto
     * @return
     */
    public static String generateBeneficiaryAddress(BeneficiaryDto beneficiaryDto) {
        return StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine1(), "") + COMMA +
                StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine2(), "") + COMMA +
                StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine3(), "");
    }


    /**
     *
     * @param customerDetails
     * @return
     */
    public static String getMobileNumber(CustomerDetailsDto customerDetails) {
        return customerDetails.getPhones().stream()
                .filter(phone -> PHONE_NUMBER_TYPES.equals(phone.getPhoneNumberType()) && StringUtils.isNotBlank(phone.getMobNumber()))
                .findFirst()
                .map(phone -> phone.getMobNumber()).orElse("");
    }

    /**
     *
     * @param remitFundTransferRequest
     * @param customerDetails
     * @return
     */
    public static QuickRemitFundTransferRequest deriveSenderIdNumberAndAddress(QuickRemitFundTransferRequest remitFundTransferRequest, CustomerDetailsDto customerDetails) {
        String address = deriveAddress(customerDetails.getAddress());
            return remitFundTransferRequest.toBuilder().senderIDType(customerDetails.getUniqueIDName())
                    .senderIDNumber(customerDetails.getUniqueIDValue()).senderAddress(address).build();

    }

    private static String generateSenderAddress(AddressTypeDto addressTypeDto) {
        return StringUtils.defaultIfBlank(addressTypeDto.getAddress1(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress2(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress3(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress4(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress5(), "") + COMMA;
    }



    private static String deriveAddress(List<AddressTypeDto> address) {
        final Optional<AddressTypeDto> first = address.stream().filter(a -> ADDRESS_TYPES.contains(a.getAddressType())).findFirst();
        return first.map(add -> generateSenderAddress(add)).orElse("");
    }

}
