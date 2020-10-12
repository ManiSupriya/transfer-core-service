package com.mashreq.transfercoreservice.transactionqueue;

import com.mashreq.transfercoreservice.dto.CharityPaidDto;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

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
        if(tmpObj.isEmpty())
            return CharityPaidDto.builder().totalPaidAmount(totalPaidAmount).build();

        log.info("[TransactionHistoryService] charity paid found for cif={} and serviceType={}", htmlEscape(cifId), htmlEscape(serviceType));

        Object[] resultSet = tmpObj.get(0);
        totalPaidAmount =  (BigDecimal) resultSet[0];
        return CharityPaidDto.builder().totalPaidAmount(totalPaidAmount).build();
    }
    

    public boolean isFinancialTransactionPresent(String financialTransactionNo) {
        return transactionRepository.existsPaymentHistoryByFinancialTransactionNo(financialTransactionNo);
    }
}
