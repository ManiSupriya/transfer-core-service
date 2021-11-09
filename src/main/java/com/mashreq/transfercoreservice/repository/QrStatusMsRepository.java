package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.QuickRemitStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrStatusMsRepository extends JpaRepository<QuickRemitStatusMaster, Long>  {

}
