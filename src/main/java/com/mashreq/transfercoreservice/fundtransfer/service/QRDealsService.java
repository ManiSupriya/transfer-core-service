package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.QRDealDetails;

public interface QRDealsService {

    QRDealDetails getQRDealDetails(String cif, String country);

    void updateQRDeals(String cif, Integer utilizedAmount);
}
