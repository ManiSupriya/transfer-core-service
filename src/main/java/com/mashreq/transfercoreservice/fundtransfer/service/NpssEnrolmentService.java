package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepo;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentStatusResponseDTO;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NpssEnrolmentService {
    private static final String NPSS_ENROLLED = "ENROLLED";

    public NpssEnrolmentStatusResponseDTO checkEnrolment(RequestMetaData metaData) {
        NpssEnrolmentRepo npssEnrolmentRepo = npssEnrolmentRepository.getEnrolmentStatus(metaData.getPrimaryCif());
        return NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(
                npssEnrolmentRepo == null || npssEnrolmentRepo.getEnrollment_status() != NPSS_ENROLLED
        ).build();
    }
//    public NpssEnrolmentStatusResponseDTO updateUserEnrolment(RequestMetaData metaData) {
//        NpssEnrolmentStatusResponseDTO response = new NpssEnrolmentStatusResponseDTO();
//        NpssEnrolmentRepo npssEnrolmentRepo = npssEnrolmentRepository.updateEnrolmentStatus(metaData.getPrimaryCif());
//        response.setAskForEnrolment(npssEnrolmentRepo == null);
//        return response;
//    }
}
