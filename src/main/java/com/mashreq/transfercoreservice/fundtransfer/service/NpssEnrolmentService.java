package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.dto.HandleNotificationRequestDto;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.enums.AccountType;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DB_CONNECTIVITY_ISSUE;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.PAYMENT_SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class NpssEnrolmentService {
    private static final String NPSS_ENROLLED = "ENROLLED";

    private static final String CURRENCY = "AED";
    private static final String ACTIVE = "ACTIVE";

    private static final String SCHEME_TYPE_SA = "SA";

    private static final String SCHEME_TYPE_CA = "CA";
    private final NpssEnrolmentRepository npssEnrolmentRepository;
    private final FundTransferServiceDefault fundTransferServiceDefault;
    private final DigitalUserService digitalUserService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final NpssNotificationService npssNotificationService;
    private final AccountService accountService;

    private final TransactionHistoryService transactionHistoryService;


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
        log.info("Consent process started fro the cif {}", metaData.getPrimaryCif());
        NpssEnrolmentRepoDTO npssEnrolmentNewEntry = NpssEnrolmentRepoDTO.builder()
                .cif_id(metaData.getPrimaryCif())
                .enrollment_status(NPSS_ENROLLED)
                .accepted_date(Instant.now())
                .build();
        try {
            npssEnrolmentRepository.save(npssEnrolmentNewEntry);
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(true).build();
        } catch (Exception err) {
            log.error("Consent Details save Failed ", err);
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(false).build();
        }
    }

    public void handleTransaction(RequestMetaData requestMetaData, HandleNotificationRequestDto handleNotificationRequestDto) {
        UserDTO userDTO = getUserDetailsFromMetaData(requestMetaData);
        if (handleNotificationRequestDto.getNotificationRequestDto().getNotificationType().equals(PAYMENT_SUCCESS)) {
            digitalUserLimitUsageService.insert(fundTransferServiceDefault.generateUserLimitUsage("LOCAL",
                    handleNotificationRequestDto.getNotificationRequestDto().getAmount(), userDTO, requestMetaData, handleNotificationRequestDto.getNotificationRequestDto().getLimitVersionUuid(), handleNotificationRequestDto.getNotificationRequestDto().getReferenceNumber(), null
            ));
        }
        transactionHistoryService.saveTransactionHistory(handleNotificationRequestDto.getTransactionHistoryDto(),requestMetaData);
        npssNotificationService.performNotificationActivities(requestMetaData, handleNotificationRequestDto.getNotificationRequestDto(), userDTO);
    }


    public void performNotificationActivities(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto) {
        npssNotificationService.performNotificationActivities(requestMetaData, notificationRequestDto, getUserDetailsFromMetaData(requestMetaData));
    }

    private UserDTO getUserDetailsFromMetaData(RequestMetaData requestMetaData) {
        log.info("Finding Digital User for CIF-ID {}", htmlEscape(requestMetaData.getPrimaryCif()));
        DigitalUser digitalUser = digitalUserService.getDigitalUser(requestMetaData);

        log.info("Creating  User DTO");
        UserDTO userDTO = fundTransferServiceDefault.createUserDTO(requestMetaData, digitalUser);
        log.info("Save Digital Limit Usage  for Cif{} ", htmlEscape(requestMetaData.getPrimaryCif()));
        return userDTO;
    }
}
