package com.mashreq.transfercoreservice.limits;

import com.mashreq.transfercoreservice.model.DigitalUserLimitUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DigitalUserLimitUsageRepository extends JpaRepository<DigitalUserLimitUsage, Long> {

    /**
     *
     */
    @Query(value = "SELECT count(*) as cnt, SUM(du.paidAmount) as amount FROM DigitalUserLimitUsage as du WHERE " +
            "du.cif = :cif and " +
            "du.beneficiaryTypeCode = :beneficiaryTypeCode and " +
            "convert(nvarchar(7), du.createdDate, 23) = :yearMonth " +
            "GROUP BY du.cif")
    List<Object[]> getMonthlyLimitUsageByBillerTypeAndCif(@Param("beneficiaryTypeCode") String beneficiaryTypeCode,
                                                          @Param("cif") String cif,
                                                          @Param("yearMonth") String yearMonth);

    /**
     *
     */
    @Query(value = "SELECT count(*) as cnt, SUM(du.paidAmount) as amount FROM DigitalUserLimitUsage as du WHERE " +
            "du.cif = :cif and " +
            "du.beneficiaryTypeCode = :beneficiaryTypeCode and " +
            "convert(nvarchar(10), du.createdDate, 23) = :todayDate " +
            "GROUP BY du.cif")
    List<Object[]> getDailyLimitUsageByBillerTypeAndCif(@Param("beneficiaryTypeCode") String beneficiaryTypeCode,
                                                        @Param("cif") String cif,
                                                        @Param("todayDate") String todayDate);
}
