package com.mashreq.transfercoreservice.fundtransfer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.model.EscrowAccountDetails;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccountDetails, Long> {

    Optional<EscrowAccountDetails> findByAccountNo(String accountNo);
}