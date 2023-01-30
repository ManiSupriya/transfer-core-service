package com.mashreq.transfercoreservice.transactionqueue;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionHistory, Long> {
	
	boolean existsPaymentHistoryByFinancialTransactionNo(String financialTransactionNo);

	boolean existsPaymentHistoryByTransactionRefNo(String transactionRefNo);
	
	 @Query(value = "SELECT SUM(th.paidAmount) as amount FROM TransactionHistory as th WHERE " +
	            "th.cif = :cif and " +
	            "th.transactionTypeCode = :transactionTypeCode and " +
	            "th.status = :status  " +
	            "GROUP BY th.cif")
	    List<Object[]> findSumByCifIdAndServiceType(@Param("cif") String cif,@Param("transactionTypeCode") String transactionTypeCode,@Param("status") String status);
	TransactionHistory findByHostReferenceNo(final String hostReferenceNo);
   
}