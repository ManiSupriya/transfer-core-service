package com.mashreq.transfercoreservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.StringJoiner;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class TransferRule {

    private int id;
    private boolean routingCodeRequired;
    private boolean ibanRequired;
    private boolean addressRequired;
    private Integer maxLength;
    private String routingTypeCode;
    private String routingCodeName;

    @Override
    public String toString() {
        return new StringJoiner(", ", TransferRule.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("routingCodeRequired=" + routingCodeRequired)
                .add("ibanRequired=" + ibanRequired)
                .add("addressLine1Required=" + addressRequired)
                .add("maxLength=" + maxLength)
                .add("routingTypeCode='" + routingTypeCode + "'")
                .add("routingCodeName='" + routingCodeName + "'")
                .toString();
    }
}