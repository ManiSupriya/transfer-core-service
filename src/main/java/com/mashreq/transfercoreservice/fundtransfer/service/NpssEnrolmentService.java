package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentStatusResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentUpdateResponseDTO;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class NpssEnrolmentService {
    private static final String NPSS_ENROLLED = "ENROLLED";
    private final NpssEnrolmentRepository npssEnrolmentRepository;

    public NpssEnrolmentStatusResponseDTO checkEnrolment(RequestMetaData metaData) {
        Optional<NpssEnrolmentRepoDTO> npssEnrolmentResponse = npssEnrolmentRepository.getEnrolmentStatus(
                metaData.getPrimaryCif()
        );
        if (npssEnrolmentResponse.isPresent()) {
            return NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(false).build();
        }
        return NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(true).build();
    }
    public NpssEnrolmentUpdateResponseDTO updateEnrolment(RequestMetaData metaData) {
        NpssEnrolmentRepoDTO npssEnrolmentNewEntry = NpssEnrolmentRepoDTO.builder()
                .cif_id(metaData.getPrimaryCif())
                .enrollment_status(NPSS_ENROLLED)
                .accepted_date(java.time.LocalDateTime.now())
                .build();
        try{
            npssEnrolmentRepository.save(npssEnrolmentNewEntry);
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(true).build();
        }catch (Exception err){
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(false).build();
        }
    }
}
