package com.mashreq.transfercoreservice.mapper;

import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistory;

public class TransactionHistoryMapper {

    public static TransactionHistoryDto getTransactionHistoryDto(final TransactionHistory transactionHistory) {
        return TransactionHistoryDto.builder()
                .accountFrom(transactionHistory.getAccountFrom())
                .accountTo(transactionHistory.getAccountTo())
                .beneficiaryId(transactionHistory.getBeneficiaryId())
                .transactionTypeCode(transactionHistory.getTransactionTypeCode())
                .billRefNo(transactionHistory.getBillRefNo())
                .channel(transactionHistory.getChannel())
                .cif(transactionHistory.getCif())
                .dueAmount(transactionHistory.getDueAmount())
                .fromCurrency(transactionHistory.getFromCurrency())
                .paidAmount(transactionHistory.getPaidAmount())
                .toCurrency(transactionHistory.getToCurrency())
                .fromCurrency(transactionHistory.getFromCurrency())
                .financialTransactionNo(transactionHistory.getFinancialTransactionNo())
                .hostReferenceNo(transactionHistory.getHostReferenceNo())
                .dealNumber(transactionHistory.getDealNumber())
                .valueDate(transactionHistory.getValueDate())
                .countryCode(transactionHistory.getCountryCode())
                .transferPurpose(transactionHistory.getTransferPurpose())
                .transactionRefNo(transactionHistory.getTransactionRefNo())
                .transactionCategory(transactionHistory.getTransactionCategory())
                .createdDate(transactionHistory.getCreatedDate())
                .debitAmount(transactionHistory.getDebitAmount())
                .exchangeRate(transactionHistory.getExchangeRate())
                .build();
    }
}
