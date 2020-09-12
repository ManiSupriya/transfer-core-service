package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.QRDealsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QRDealRepository extends JpaRepository<QRDealsEntity, Long> {

    @Query(value = "select top 1 * from (SELECT * FROM qr_deals_details_staging WHERE cif = :cif and eligible_countries like %:country% " +
            " and CURRENT_TIMESTAMP between eligibility_start_date and eligibility_end_date union "+
            " SELECT * FROM qr_deals_details WHERE cif = :cif and eligible_countries like %:country% "+
            " and CURRENT_TIMESTAMP between eligibility_start_date and eligibility_end_date) as qr_deals order by created_on desc ", nativeQuery = true)
    QRDealsEntity findDealsByCIFAndCountry(@Param("cif") String cif, @Param("country") String country);

    QRDealsEntity findDealsByCif(@Param("cif") String cif);
}