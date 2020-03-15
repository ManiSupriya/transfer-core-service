package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.payments.billpayments.dto.PaymentHistoryDTO;
import com.mashreq.payments.billpayments.mapper.PaymentHistoryMapper;
import com.mashreq.payments.model.PaymentHistory;
import com.mashreq.payments.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentHistoryServiceDefault implements PaymentHistoryService {

    private final PaymentHistoryMapper paymentHistoryMapper;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Override
    public void insert(PaymentHistoryDTO paymentHistoryDTO) {
        log.info("Store payment history for cif={} and billertype ", paymentHistoryDTO.getCif());
        PaymentHistory paymentHistory = paymentHistoryMapper.paymentHistoryDtoToEntity(paymentHistoryDTO);
        paymentHistoryRepository.save(paymentHistory);
    }
}
