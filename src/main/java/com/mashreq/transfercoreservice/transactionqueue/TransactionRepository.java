package com.mashreq.transfercoreservice.transactionqueue;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionHistory, Long> {

   
}