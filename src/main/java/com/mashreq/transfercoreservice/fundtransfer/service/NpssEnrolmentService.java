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
        try {
            log.info("Requesting for this CIF {} ", htmlEscape(metaData.getPrimaryCif()));
            log.info("DataBase response isPresent", htmlEscape(npssEnrolmentResponse.isPresent()));
            log.info("DataBase response get", htmlEscape(npssEnrolmentResponse.get()));
        }catch(Exception err){
            log.info("Exception", htmlEscape(err));
        }

        if (npssEnrolmentResponse.isPresent()) {
            NpssEnrolmentRepoDTO npssEnrollment = npssEnrolmentResponse.get();
            log.info("DataBase response npssEnrollment", htmlEscape(npssEnrollment));
            return NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(npssEnrollment == null).build();
        }
        return NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(false).build();
    }
    public NpssEnrolmentUpdateResponseDTO updateEnrolment(RequestMetaData metaData) {

        Optional<NpssEnrolmentRepoDTO> npssEnrolmentResponse = npssEnrolmentRepository.updateEnrolmentStatus(
                metaData.getPrimaryCif()
        );
        return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(true).build();
    }
}
