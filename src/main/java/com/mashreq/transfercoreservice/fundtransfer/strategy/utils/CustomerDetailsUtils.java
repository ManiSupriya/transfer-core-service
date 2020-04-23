package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.transfercoreservice.client.dto.AddressTypeDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CustomerDetailsDto;
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
        return StringUtils.defaultIfBlank(customerDetails.getMobile(), customerDetails.getPrimaryPhoneNumber());
    }

    /**
     *
     * @param remitFundTransferRequest
     * @param customerDetails
     * @return
     */
    public static QuickRemitFundTransferRequest deriveSenderIdNumberAndAddress(QuickRemitFundTransferRequest remitFundTransferRequest, CustomerDetailsDto customerDetails) {
        String address = deriveAddress(customerDetails.getAddress());

        if (StringUtils.isNotBlank(customerDetails.getNationalNumber())) {
            return remitFundTransferRequest.toBuilder().senderIDType("NATIONAL ID")
                    .senderIDNumber(customerDetails.getNationalNumber()).senderAddress(address).build();
        } else if (StringUtils.isNotBlank(customerDetails.getPassportNumber())) {
            return remitFundTransferRequest.toBuilder().senderIDType("PASSPORT ID")
                    .senderIDNumber(customerDetails.getPassportNumber()).senderAddress(address).build();
        } else {
            return remitFundTransferRequest.toBuilder().senderIDType("VISA ID")
                    .senderIDNumber(customerDetails.getVisaNumber()).senderAddress(address).build();
        }
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
