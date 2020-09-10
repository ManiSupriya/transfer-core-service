package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Used to hold the QR Deals Details
 * @author ThanigachalamP
 */
@Getter @Setter
public class QRDealDetails implements Serializable {

    private String cif;

    private String eligibleCountries;

    private Integer totalLimitAmount;

    private Float utilizedLimitAmount;

    private Date eligibilityStartDate;

    private Date eligibilityEndDate;
}
