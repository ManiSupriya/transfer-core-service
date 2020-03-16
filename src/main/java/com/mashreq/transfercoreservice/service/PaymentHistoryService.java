package com.mashreq.transfercoreservice.service;

import com.mashreq.transfercoreservice.dto.CharityPaidDto;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    /**
     * Service to get the charity paid by a given CIF and charity type
     */
    public CharityPaidDto getCharityPaid(String cifId, String serviceType) {

        log.info("[PaymentHistoryService] get charity paid for cif={} and serviceType={}", cifId, serviceType);

        BigDecimal totalPaidAmount = new BigDecimal(0);

        List<Object[]> tmpObj = paymentHistoryRepository.findSumByCifIdAndServiceType(cifId, serviceType, MwResponseStatus.S.name());
        if(tmpObj.isEmpty())
            return CharityPaidDto.builder().totalPaidAmount(totalPaidAmount).build();

        log.info("[PaymentHistoryService] charity paid found for cif={} and serviceType={}", cifId, serviceType);

        Object[] resultSet = tmpObj.get(0);
        totalPaidAmount =  (BigDecimal) resultSet[0];
        return CharityPaidDto.builder().totalPaidAmount(totalPaidAmount).build();
    }
}
