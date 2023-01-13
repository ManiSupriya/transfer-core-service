package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.enums.AccountType;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class NpssEnrolmentService {
    private static final String NPSS_ENROLLED = "ENROLLED";

    private static final String CURRENCY = "AED";
    private static final String ACTIVE = "ACTIVE";

    private static final String SCHEME_TYPE_SA= "SA";

    private static final String SCHEME_TYPE_CA= "CA";
    private final NpssEnrolmentRepository npssEnrolmentRepository;
    private final FundTransferServiceDefault fundTransferServiceDefault;
    private final DigitalUserService digitalUserService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final NPSSNotificationService npssNotificationService;
    private final AccountService accountService;


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

        // update default account with new entry
        try {
        updateDefaultAccount(metaData,false);
        /*NpssEnrolmentRepoDTO npssEnrolmentNewEntry = NpssEnrolmentRepoDTO.builder()
                .cif_id(metaData.getPrimaryCif())
                .enrollment_status(NPSS_ENROLLED)
                .accepted_date(Instant.now())
                .build();*/
//                .accepted_date("2020-12-23 15:40:45.2756145")//


            //npssEnrolmentRepository.save(npssEnrolmentNewEntry);
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(true).build();
        } catch (Exception err) {
            log.error("New Entry Enrollment Failed : ",err);
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
                notificationRequestDto.getAmount(), userDTO, requestMetaData, notificationRequestDto.getLimitVersionUuid(), notificationRequestDto.getTransactionReferenceNo(), null
        ));
        // if(isSuccessOrProcessing(fundTransferResponse)){
        //TODO: Add Service code for NPSS
        //  }
        npssNotificationService.performNotificationActivities(requestMetaData, new NotificationRequestDto(), userDTO);
    }

    public String updateDefaultAccount(RequestMetaData requestMetaData,Boolean isFromScheduler) {
        log.info("update Default Account process started");
        List<NpssEnrolmentRepoDTO> npssEnrolmentResponse = new ArrayList<>();
        if(isFromScheduler){
            npssEnrolmentResponse = npssEnrolmentRepository.findAllByIsDefaultAccountUpdated(Boolean.FALSE);
        } else {
            NpssEnrolmentRepoDTO npssEnrolmentNewEntry = NpssEnrolmentRepoDTO.builder()
                    .cif_id(requestMetaData.getPrimaryCif())
                    .enrollment_status(NPSS_ENROLLED)
                    .accepted_date(Instant.now())
                    .build();
            npssEnrolmentResponse.add(npssEnrolmentNewEntry);
        }
        npssEnrolmentResponse.forEach(npssEnrolmentRepoDTO -> {
            List<AccountDetailsDTO> accountDetails = accountService.getAccountsFromCore(npssEnrolmentRepoDTO.getCif_id());
            List<AccountDetailsRepoDTO> accountDetailsRepo = prepareBankDetails(accountDetails, npssEnrolmentRepoDTO.getCif_id());
            npssEnrolmentRepoDTO.setAccountDetails(accountDetailsRepo);
            npssEnrolmentRepoDTO.set_default_account_updated(Boolean.TRUE);
            try {
                npssEnrolmentRepository.save(npssEnrolmentRepoDTO);
            } catch (Exception e) {
                GenericExceptionHandler.handleError(DB_CONNECTIVITY_ISSUE, DB_CONNECTIVITY_ISSUE.getErrorMessage());
            }

        });
        return "";
    }

    private List<AccountDetailsRepoDTO> prepareBankDetails(final List<AccountDetailsDTO> accountDetails, final String cifId) {
        AtomicBoolean isDefaultAdded = new AtomicBoolean(Boolean.FALSE);
        final Map<String, List<AccountDetailsDTO>> accountDetailsDTOs = accountDetails.stream()
                .filter(accountDetail -> {
                    return accountDetail.getCurrency().equalsIgnoreCase(CURRENCY)
                            && accountDetail.getStatus().equalsIgnoreCase(ACTIVE);
                }).collect(Collectors.groupingBy(AccountDetailsDTO::getSchemeType));
        List<AccountDetailsRepoDTO> accountDetailsRepoDTO = new ArrayList<>();
        if (accountDetailsDTOs.containsKey(SCHEME_TYPE_SA)) {
            buildAccountDetailsRepoDTO(accountDetailsDTOs.get(SCHEME_TYPE_SA), accountDetailsRepoDTO, cifId,isDefaultAdded);
        }
        if (accountDetailsDTOs.containsKey(SCHEME_TYPE_CA)) {
            buildAccountDetailsRepoDTO(accountDetailsDTOs.get(SCHEME_TYPE_CA), accountDetailsRepoDTO, cifId,isDefaultAdded);
        }
        buildAccountDetailsRepoDTO(accountDetailsDTOs
                .entrySet().stream()
                .filter(accountDetail -> !accountDetail.getKey().equalsIgnoreCase(SCHEME_TYPE_SA)
                        && !accountDetail.getKey().equalsIgnoreCase(SCHEME_TYPE_CA))
                .map(Map.Entry::getValue).flatMap(List::stream)
                .collect(Collectors.toList()), accountDetailsRepoDTO, cifId,isDefaultAdded);
        return accountDetailsRepoDTO;
    }

    private void buildAccountDetailsRepoDTO(final List<AccountDetailsDTO> accountDetails,
                                            final List<AccountDetailsRepoDTO> accountDetailsRepoDTO,
                                            final String cifId, final AtomicBoolean isDefaultAdded) {
        accountDetails.forEach(accountDetail -> {
            accountDetailsRepoDTO.add(AccountDetailsRepoDTO.builder()
                    .accountName(accountDetail.getAccountName())
                    .status(accountDetail.getStatus())
                    .type(isDefaultAdded.get() ? AccountType.OTHERS.name() : AccountType.DEFAULT.name())
                    .branchCode(accountDetail.getBranchCode())
                    .schemeType(accountDetail.getSchemeType()).currency(accountDetail.getCurrency())
                    .segment(accountDetail.getSegment()).accountType(accountDetail.getAccountType())
                    .cifRef(cifId)
                    .accountNumber(accountDetail.getNumber()).build());
            isDefaultAdded.set(Boolean.TRUE);
        });
    }
}
