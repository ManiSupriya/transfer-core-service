package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.HandleTransactionRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
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

        try{
            npssEnrolmentRepository.save(npssEnrolmentNewEntry);
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(true).build();
        }catch (Exception err){
            return NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(false).build();
        }
    }

    public void handleTransaction(RequestMetaData requestMetaData, HandleTransactionRequestDto handleTransactionRequestDto){

        log.info("Starting Handle Transaction{} ", htmlEscape(handleTransactionRequestDto));
        log.info("Finding Digital User for CIF-ID {}", htmlEscape(requestMetaData.getPrimaryCif()));
        DigitalUser digitalUser = fundTransferServiceDefault.getDigitalUser(requestMetaData);
        FundTransferRequestDTO fundTransferRequestDTO = generateFundTransferRequestDto(handleTransactionRequestDto);
        FundTransferResponse fundTransferResponse = generateFundTransferResponse(handleTransactionRequestDto);
        log.info("Creating  User DTO");
        UserDTO userDTO = fundTransferServiceDefault.createUserDTO(requestMetaData, digitalUser);
        fundTransferServiceDefault.handleIfTransactionIsSuccess(requestMetaData,fundTransferRequestDTO,userDTO,fundTransferResponse);
    }

    private FundTransferRequestDTO generateFundTransferRequestDto( HandleTransactionRequestDto handleTransactionRequestDto){
        return null;
    }
    private FundTransferResponse generateFundTransferResponse( HandleTransactionRequestDto handleTransactionRequestDto){
        return null;
    }
}
