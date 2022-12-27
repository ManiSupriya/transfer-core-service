package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentStatusResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentUpdateResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class NpssEnrolmentService {
    private static final String NPSS_ENROLLED = "ENROLLED";
    private final NpssEnrolmentRepository npssEnrolmentRepository;
    private final FundTransferServiceDefault fundTransferServiceDefault;
    private final DigitalUserService digitalUserService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final NPSSNotificationService npssNotificationService;



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
                .accepted_date(Instant.now())
                .build();
//                .accepted_date("2020-12-23 15:40:45.2756145")//

        try {
            npssEnrolmentRepository.save(npssEnrolmentNewEntry);
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(true).build();
        } catch (Exception err) {
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(false).build();
        }
    }

    public void handleTransaction(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto) {

        log.info("Finding Digital User for CIF-ID {}", htmlEscape(requestMetaData.getPrimaryCif()));
        DigitalUser digitalUser = digitalUserService.getDigitalUser(requestMetaData);

        log.info("Creating  User DTO");
        UserDTO userDTO = fundTransferServiceDefault.createUserDTO(requestMetaData, digitalUser);
        log.info("Save Digital Limit Usage  for Cif{} ", htmlEscape(requestMetaData.getPrimaryCif()));
        digitalUserLimitUsageService.insert(fundTransferServiceDefault.generateUserLimitUsage("LOCAL",
                notificationRequestDto.getAmount(),userDTO,requestMetaData,notificationRequestDto.getLimitVersionUuid(),notificationRequestDto.getTransactionReferenceNo(),null
                ));
        // if(isSuccessOrProcessing(fundTransferResponse)){
        //TODO: Add Service code for NPSS
        //  }
        npssNotificationService.performNotificationActivities(requestMetaData,new NotificationRequestDto(),userDTO);
    }
}
