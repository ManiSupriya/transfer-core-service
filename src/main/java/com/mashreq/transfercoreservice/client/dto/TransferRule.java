package com.mashreq.transfercoreservice.client.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.StringJoiner;

@Entity
@Table(name = "country_transfer_rules")
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class TransferRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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