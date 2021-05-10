package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;


@Data
public class TransferSupportedCountryDto {

    private Integer id;
    private String code;
    private String name;
    private Boolean active;
    private String phoneCode;
    private Boolean ibanRequired;
    private Integer ibanLength;
    private String routingCode;
    private Integer routingCodeLength;
    private String routingCodeRegex;
    private String nativeCurrency;
    private Boolean quickRemitEnabled;
    private String channel;
    private String region;
}

