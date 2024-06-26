package com.mashreq.transfercoreservice.paylater.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.paylater.model.FundTransferOrder;

@Repository
public interface FundTransferOrderRepository extends JpaRepository<FundTransferOrder, Long>{
	boolean existsFundTransferOrderByFinancialTransactionNo(String financialTransactionNo);
	@Query(value = "select NEXT VALUE FOR FT_ORDERID_SEQ",nativeQuery = true)
	Long getNextFTOrderSequence();
}
