package com.mashreq.transfercoreservice.client.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class IbanDetailsDto implements Serializable {
    private String ibanNo;
    private String branch;
    private String accountNo;
}
