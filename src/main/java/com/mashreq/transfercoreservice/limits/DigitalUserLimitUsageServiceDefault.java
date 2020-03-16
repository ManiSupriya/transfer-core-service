package com.mashreq.transfercoreservice.limits;


import com.mashreq.transfercoreservice.fundtransfer.dto.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserLimitUsageDTO;
import com.mashreq.transfercoreservice.model.DigitalUserLimitUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalUserLimitUsageServiceDefault implements DigitalUserLimitUsageService {

    private final DigitalUserLimitUsageMapper digitalUserLimitUsageMapper;
    private final DigitalUserLimitUsageRepository digitalUserLimitUsageRepository;

    @Override
    public void insert(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO) {
        log.info("Store limit usage for CIF={} and beneficiaryTypeCode={} ",
                digitalUserLimitUsageDTO.getCif(), digitalUserLimitUsageDTO.getDigitalUserId());
        DigitalUserLimitUsage digitalUserLimitUsage = digitalUserLimitUsageMapper.userLimitUsageDTOToEntity(digitalUserLimitUsageDTO);
        digitalUserLimitUsageRepository.save(digitalUserLimitUsage);
    }

    @Override
    public Optional<UserLimitUsageDTO> getMonthlyLimitUsageByBillerTypeAndCif(String beneficiaryTypeCode, String cif) {
        log.info("Get monthly limit usage for beneficiaryTypeCode={} and CIF={} ", beneficiaryTypeCode, cif);

        String yearMonth = new SimpleDateFormat("yyyy-MM").format(new Date());

        List<Object[]> tmpObj = digitalUserLimitUsageRepository.getMonthlyLimitUsageByBillerTypeAndCif(beneficiaryTypeCode, cif, yearMonth);
        if (tmpObj.isEmpty())
            return Optional.ofNullable(null);

        Object[] resultSet = tmpObj.get(0);
        UserLimitUsageDTO userLimitUsageDTO = UserLimitUsageDTO.builder()
                .usedCount(((Long) resultSet[0]).intValue())
                .usedAmount((BigDecimal) resultSet[1])
                .build();
        return Optional.ofNullable(userLimitUsageDTO);
    }

    @Override
    public Optional<UserLimitUsageDTO> getDailyLimitUsageByBillerTypeAndCif(String beneficiaryTypeCode, String cif) {
        log.info("Get daily limit usage for beneficiaryTypeCode={} and CIF={} ", beneficiaryTypeCode, cif);

        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        List<Object[]> tmpObj = digitalUserLimitUsageRepository.getDailyLimitUsageByBillerTypeAndCif(beneficiaryTypeCode, cif, todayDate);
        if (tmpObj.isEmpty())
            return Optional.ofNullable(null);

        Object[] resultSet = tmpObj.get(0);
        UserLimitUsageDTO userLimitUsageDTO = UserLimitUsageDTO.builder()
                .usedCount(((Long) resultSet[0]).intValue())
                .usedAmount((BigDecimal) resultSet[1])
                .build();
        return Optional.ofNullable(userLimitUsageDTO);
    }


//    NOTE: PLEASE KEEP BELOW CODE TILL LIMIT MANAGEMENT IS FULLY FINALISE
//
//    /**
//     * check if the dayCount, monthCount, dailyAmount and monthlyAmount with respect to last modifiedDate
//     * If now is same with last modified date increment with dayCount and dailyAmount
//     * If now month same with last modified date month increment with monthCount and monthAmount
//     */
//    @Override
//    public void updateUsageLimitForBillerTypeAndUser(BillPaymentReqDTO reqDTO) {
//
//        //Get the record from the usage DB table for given beneficiary type code and CIF/user
//        Optional<UserLimitUsageDTO> usedLimit = getLimitUsageByBillerTypeAndUser();
//        if(usedLimit.isPresent()){
//            log.info("User usage exists for given payment type and user ", reqDTO.getPaymentType());
//            // update
//            Instant todayDateInstant = Instant.now();
//            Instant lastModifiedDateInstant = usedLimit.get().getLastModifiedDate();
//
//            if(checkIfDateEqual(todayDateInstant, lastModifiedDateInstant)){
//                //TODO: set daycount and dayamount incremental
//            }
//
//            if(checkIfMonthYearEqual(todayDateInstant, lastModifiedDateInstant)){
//                //TODO: set monthcount and monthamount incremental
//            }
//        } else {
//            log.info("User usage NOT exists for given payment type and user ", reqDTO.getPaymentType());
//
//            // insert
//        }
//    }
//
//    /**
//     * Check if the date of current month
//     */
//    private boolean checkIfMonthYearEqual(Instant todayDateInstant, Instant lastModifiedDateInstant) {
//        return Month.from(todayDateInstant) == Month.from(lastModifiedDateInstant) &&
//                Year.from(todayDateInstant).equals(Year.from(lastModifiedDateInstant));
//    }
//
//    /**
//     * Check if given date is today date
//     */
//    private boolean checkIfDateEqual(Instant todayDateInstant, Instant lastModifiedDateInstant){
//        LocalDate todayDate = LocalDateTime.ofInstant(todayDateInstant, ZoneId.systemDefault()).toLocalDate();
//        LocalDate lastModifiedDate = LocalDateTime.ofInstant(lastModifiedDateInstant, ZoneId.systemDefault()).toLocalDate();
//
//        return todayDate.isEqual(lastModifiedDate);
//    }
//
//    @Override
//    public Optional<UserLimitUsageDTO> getLimitUsageByBillerTypeAndUser() {
//        return Optional.empty();
//    }
}
