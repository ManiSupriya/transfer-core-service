package com.mashreq.transfercoreservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
@Repository
public interface BankRepository extends JpaRepository<BankDetails, Long>  {

	@Query(value = "SELECT bankCode from BankDetails where countryCode=:country and " +
			"(swiftCode = :swiftCode or swiftCode = substring(:swiftCode,1,8))")
    Optional<String> getBankCode(String country, String swiftCode);

	Optional<BankDetails> findByBankCode(String bankCode);

}
