package com.mashreq.transfercoreservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
@Repository
public interface BankRepository extends JpaRepository<BankDetails, Long>  {

	@Query(value = "SELECT b from BankDetails b where countryCode=:country and " +
			"(swiftCode = :swiftCode or swiftCode like :swiftCodeLengthModified)")
    Optional<BankDetails> getBankCode(String country, String swiftCode,String swiftCodeLengthModified);

	Optional<BankDetails> findByBankCode(String bankCode);

}
