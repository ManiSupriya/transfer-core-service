package com.mashreq.transfercoreservice.event.repository;

import com.mashreq.transfercoreservice.event.entity.UserEventAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventAuditRepository extends JpaRepository<UserEventAudit, Long> {

}
