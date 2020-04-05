package com.mashreq.transfercoreservice.fundtransfer.service;


import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.model.PaymentHistory;
import com.mashreq.transfercoreservice.paymenthistory.PaymentHistoryMapper;
import com.mashreq.transfercoreservice.repository.PaymentHistoryRepository;
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
        log.info("Store payment history for cif = {} and beneficiary type {} ", paymentHistoryDTO.getCif(), paymentHistoryDTO.getBeneficiaryTypeCode());
        PaymentHistory paymentHistory = paymentHistoryMapper.paymentHistoryDtoToEntity(paymentHistoryDTO);
        paymentHistoryRepository.save(paymentHistory);
    }

    @Override
    public boolean isFinancialTransactionPresent(String financialTransactionNo) {
        return paymentHistoryRepository.existsPaymentHistoryByFinancialTransactionNo(financialTransactionNo);
    }
}
