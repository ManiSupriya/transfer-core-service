
package com.mashreq.transfercoreservice.transactionqueue;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.dto.CharityPaidDto;
import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.mapper.TransactionHistoryMapper;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.mashreq.transfercoreservice.common.CommonConstants.MOB_CHANNEL;
import static com.mashreq.transfercoreservice.common.CommonConstants.NPSS;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DB_CONNECTIVITY_ISSUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

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
        log.info("Save data into transaction history table for idSct {}",transactionHistoryDto.getHostReferenceNo());
        TransactionHistory transactionHistory = TransactionHistory.builder().cif(metaData.getPrimaryCif()).
                userId(transactionHistoryDto.getUserId()). channel(MOB_CHANNEL).transactionTypeCode(NPSS).
                paidAmount(transactionHistoryDto.getPaidAmount()).status(transactionHistoryDto.getStatus()).
                ipAddress(metaData.getDeviceIP()).mwResponseDescription(transactionHistoryDto.getMwResponseDescription()).
                accountFrom(transactionHistoryDto.getAccountTo()).accountTo(transactionHistoryDto.getAccountTo()).
                billRefNo(transactionHistoryDto.getBillRefNo()).valueDate(LocalDateTime.now()).createdDate(Instant.now()).
                hostReferenceNo(transactionHistoryDto.getHostReferenceNo()).
                transactionRefNo(transactionHistoryDto.getTransactionRefNo()).financialTransactionNo(transactionHistoryDto.getFinancialTransactionNo()).build();
        log.info("Inserting into Transaction History table {} ", transactionHistoryDto);
        return transactionRepository.save(transactionHistory).getId();
    }

    public TransactionHistoryDto getTransactionDetailByHostRef(final String paymentId) {
        TransactionHistory transactionHistory = null;
        try {
            log.info("Querying Transaction History Details for the cif : {} ", paymentId);
            transactionHistory = transactionRepository.findByHostReferenceNo(paymentId);
            log.info("The details received from DB is : {} ", transactionHistory);
        } catch (Exception e) {
            log.error("DB Connectivity Issue ", e);
            GenericExceptionHandler.handleError(DB_CONNECTIVITY_ISSUE, DB_CONNECTIVITY_ISSUE.getErrorMessage());
        }
        return TransactionHistoryMapper.getTransactionHistoryDto(transactionHistory);
    }

    public List<TransactionHistoryDto> getTransactionHistoryByCif(final String cif,final String startDate,final String endDate) {
        List<TransactionHistory> transactionHistory = null;
        try {
            log.info("Querying Transaction History Details for the cif : {} ", cif);
            transactionHistory = transactionRepository.findAllByCifAndCreatedDate(cif,startDate,getEndDate(endDate));
            log.info("The details received from DB is : {} ", transactionHistory);
        } catch (Exception e) {
            log.error("DB Connectivity Issue ", e);
            GenericExceptionHandler.handleError(DB_CONNECTIVITY_ISSUE, DB_CONNECTIVITY_ISSUE.getErrorMessage());
        }
        return TransactionHistoryMapper.getTransactionHistoryDto(transactionHistory);
    }

    private String getEndDate(final String endDate) {
        LocalDate endDateDB = null;
        try {
            LocalDate date = LocalDate.parse(endDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
            endDateDB = date.plusDays(1);
        } catch (Exception e) {
            log.error("Date conversion Issue", e);
        }
        return Optional.ofNullable(endDateDB).isPresent() ? endDateDB.toString() : endDate;
    }
}
