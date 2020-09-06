package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.util.Map;

import lombok.Data;

@Data
public class AdditionalFields {

    Map<String, String> missingFields;
    Map<String, String> incorrectFields;

}