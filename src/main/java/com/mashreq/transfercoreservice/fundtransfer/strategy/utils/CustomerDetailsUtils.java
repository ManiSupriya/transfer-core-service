package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;


import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.AddressTypeDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerPhones;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author shahbazkh
 * @date 4/23/20
 */
public class CustomerDetailsUtils {

    private static final String DELIMTER = " ";
    private static final List<String> ADDRESS_TYPES = Arrays.asList("P", "R", "O");
    private static final List<String> PHONE_NUMBER_TYPES = Arrays.asList("P", "O");

    /**
     *
     * @param beneficiaryDto
     * @return
     */
    public static String generateBeneficiaryAddress(BeneficiaryDto beneficiaryDto) {
        return StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine1(), "") + DELIMTER +
                StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine2(), "") + DELIMTER +
                StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine3(), "");
    }


    /**
     *
     * @param customerDetails
     * @return
     */
    public static String getMobileNumber(CustomerDetailsDto customerDetails) {
        final Map<String, CustomerPhones> phoneNumByType = customerDetails.getPhones().stream()
                .collect(Collectors.toMap(CustomerPhones::getPhoneNumberType, phone -> phone));
        return PHONE_NUMBER_TYPES.stream()
                .filter(type -> phoneNumByType.containsKey(type) && StringUtils.isNotBlank(phoneNumByType.get(type).getMobNumber()))
                .map(type -> phoneNumByType.get(type))
                .findFirst()
                .map(phone -> phone.getMobNumber()).orElse("");
    }

    /**
     *
     * @param remitFundTransferRequest
     * @param customerDetails
     * @return
     */
    public static QuickRemitFundTransferRequest deriveSenderIdNumberAndAddress(QuickRemitFundTransferRequest remitFundTransferRequest,
                                                                               CustomerDetailsDto customerDetails) {
        String address = deriveAddress(customerDetails.getAddress());
            return remitFundTransferRequest.toBuilder().senderIDType(customerDetails.getUniqueIDName())
                    .senderIDNumber(customerDetails.getUniqueIDValue()).senderAddress(address).build();

    }

    private static String generateSenderAddress(AddressTypeDto addressTypeDto) {
        return StringUtils.defaultIfBlank(addressTypeDto.getAddress1(), "") + DELIMTER +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress2(), "") + DELIMTER +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress3(), "") + DELIMTER +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress4(), "") + DELIMTER +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress5(), "");
    }



    public static String deriveAddress(List<AddressTypeDto> address) {
        final Map<String, AddressTypeDto> addressByType = address.stream().collect(Collectors.toMap(AddressTypeDto::getAddressType, addr -> addr));
        final Optional<AddressTypeDto> first = ADDRESS_TYPES.stream().filter(type -> addressByType.containsKey(type)).map(key -> addressByType.get(key)).findFirst();
        return first.map(add -> generateSenderAddress(add)).orElse("");
    }

}
