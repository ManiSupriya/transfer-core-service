package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByBeneficiaryTypeCode(String beneficiaryTypeCode);

    @Query(value = "SELECT SUM(ph.paidAmount) as amount FROM PaymentHistory as ph WHERE " +
            "ph.cif = :cif and " +
            "ph.beneficiaryTypeCode = :beneficiaryTypeCode and " +
            "ph.status = :status  " +
            "GROUP BY ph.cif")
    List<Object[]> findSumByCifIdAndServiceType(String cif, String beneficiaryTypeCode, String status);
}
