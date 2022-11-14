package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepo;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentStatusResponseDTO;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NpssEnrolmentService {
    private static final String NPSS_ENROLLED = "ENROLLED";
    private final NpssEnrolmentRepository npssEnrolmentRepository;

    public NpssEnrolmentStatusResponseDTO checkEnrolment(RequestMetaData metaData) {

        Optional<NpssEnrolmentRepo> npssEnrolmentResponse = npssEnrolmentRepository.getEnrolmentStatus(metaData.getPrimaryCif());
        if (npssEnrolmentResponse.isPresent()) {
            NpssEnrolmentRepo npssEnrollment = npssEnrolmentResponse.get();
            return NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(
                    npssEnrollment == null || npssEnrollment.getEnrollment_status() != NPSS_ENROLLED
            ).build();
        }
        return NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(false).build();
    }
//    public NpssEnrolmentStatusResponseDTO updateUserEnrolment(RequestMetaData metaData) {
//        NpssEnrolmentStatusResponseDTO response = new NpssEnrolmentStatusResponseDTO();
//        NpssEnrolmentRepo npssEnrolmentRepo = npssEnrolmentRepository.updateEnrolmentStatus(metaData.getPrimaryCif());
//        response.setAskForEnrolment(npssEnrolmentRepo == null);
//        return response;
//    }
}
