package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;

public interface PaymentHistoryService {
    void insert(PaymentHistoryDTO paymentHistoryDTO);
}
