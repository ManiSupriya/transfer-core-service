package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepo;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentStatusResponseDTO;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NpssEnrolmentService {
    private static final String NPSS_ENROLLED = "ENROLLED";
    private final NpssEnrolmentRepository npssEnrolmentRepository;

    public NpssEnrolmentStatusResponseDTO checkEnrolment(RequestMetaData metaData) {
        NpssEnrolmentStatusResponseDTO response = new NpssEnrolmentStatusResponseDTO();
        NpssEnrolmentRepo npssEnrolmentRepo = npssEnrolmentRepository.getEnrolmentStatus(metaData.getPrimaryCif());
        log.info("checkEnrolment {}", metaData.getPrimaryCif());
        log.info(npssEnrolmentRepo);
        response.setAskForEnrolment(npssEnrolmentRepo == null || npssEnrolmentRepo.getEnrollment_status() != NPSS_ENROLLED);
        return response;
    }
//    public NpssEnrolmentStatusResponseDTO updateUserEnrolment(RequestMetaData metaData) {
//        NpssEnrolmentStatusResponseDTO response = new NpssEnrolmentStatusResponseDTO();
//        NpssEnrolmentRepo npssEnrolmentRepo = npssEnrolmentRepository.updateEnrolmentStatus(metaData.getPrimaryCif());
//        response.setAskForEnrolment(npssEnrolmentRepo == null);
//        return response;
//    }
}
