package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.QRDealDetails;

import java.math.BigDecimal;

public interface QRDealsService {

    QRDealDetails getQRDealDetails(String cif, String country);

    void updateQRDeals(String cif, BigDecimal utilizedAmount);
}
