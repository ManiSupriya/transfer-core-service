package com.mashreq.transfercoreservice.event.repository;

import com.mashreq.transfercoreservice.event.entity.UserEventAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventAuditRepository extends JpaRepository<UserEventAudit, Long> {

    List<UserEventAudit> findByCif(final String cif);

    List<UserEventAudit> findByUsername(final String username);
}
