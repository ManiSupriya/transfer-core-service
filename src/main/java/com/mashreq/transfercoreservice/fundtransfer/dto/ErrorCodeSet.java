package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.ms.exceptions.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCodeSet implements ErrorCode {

    FTCC0001("Error is occurred while calling middleware for Fund Transfer via CC"),
    FTCC0002("Null response from middleware for Fund Transfer via CC"),


    ;
    private String errorDesc;
}
