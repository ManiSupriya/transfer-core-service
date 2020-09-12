package com.mashreq.transfercoreservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
@Repository
public interface BankMsDAO extends JpaRepository<BankDetails, Long>  {

	@Query(value = "SELECT bankCode, branchCode from BankDetails where countryCode=:country and swiftCode = :swiftcode")
    BankDetails getBankDetails(String country, String swiftcode);
}
