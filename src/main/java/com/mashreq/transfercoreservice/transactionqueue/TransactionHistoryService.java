
package com.mashreq.transfercoreservice.transactionqueue;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.CharityPaidDto;
import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static com.mashreq.transfercoreservice.common.CommonConstants.CARD_LESS_CASH;
import static com.mashreq.transfercoreservice.common.CommonConstants.MOB_CHANNEL;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;

    /**
     * Service to get the charity paid by a given CIF and charity type
     */
    public CharityPaidDto getCharityPaid(String cifId, String serviceType) {

        log.info("[TransactionHistoryService] get charity paid for cif={} and serviceType={}", htmlEscape(cifId), htmlEscape(serviceType));

        BigDecimal totalPaidAmount = new BigDecimal(0);

        List<Object[]> tmpObj = transactionRepository.findSumByCifIdAndServiceType(cifId, serviceType, MwResponseStatus.S.getName());
        if (tmpObj.isEmpty()) return CharityPaidDto.builder().totalPaidAmount(totalPaidAmount).build();

        log.info("[TransactionHistoryService] charity paid found for cif={} and serviceType={}", htmlEscape(cifId), htmlEscape(serviceType));

        Object[] resultSet = tmpObj.get(0);
        totalPaidAmount = (BigDecimal) resultSet[0];
        return CharityPaidDto.builder().totalPaidAmount(totalPaidAmount).build();
    }


    public boolean isFinancialTransactionPresent(String financialTransactionNo) {
        return transactionRepository.existsPaymentHistoryByFinancialTransactionNo(financialTransactionNo);
    }

    public Long saveTransactionHistory(TransactionHistoryDto transactionHistoryDto, RequestMetaData metaData) {
        TransactionHistory transactionHistory = TransactionHistory.builder().cif(metaData.getPrimaryCif()).
                userId(transactionHistoryDto.getUserId()).channel(MOB_CHANNEL).transactionTypeCode(CARD_LESS_CASH).
                paidAmount(transactionHistoryDto.getPaidAmount()).status(transactionHistoryDto.getStatus().toString()).
                ipAddress(metaData.getDeviceIP()).mwResponseDescription(transactionHistoryDto.getMwResponseDescription()).
                accountFrom(transactionHistoryDto.getAccountTo()).accountTo(transactionHistoryDto.getAccountTo()).
                billRefNo(transactionHistoryDto.getBillRefNo()).valueDate(LocalDateTime.now()).createdDate(Instant.now()).
                transactionRefNo(transactionHistoryDto.getTransactionRefNo()).financialTransactionNo(transactionHistoryDto.getFinancialTransactionNo()).build();
        log.info("Inserting into Transaction History table {} ", htmlEscape(transactionHistory.getTransactionRefNo()));
        return transactionRepository.save(transactionHistory).getId();
    }
}
