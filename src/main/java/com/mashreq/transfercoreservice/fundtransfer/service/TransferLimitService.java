package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.repository.TransferLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferLimitService {

    private final TransferLimitRepository repository;

    public TransferLimitResponseDto validateAndSaveTransferDetails(TransferLimitRequestDto limitRequestDto,
                                                                   String transactionRefNo) {
        try {
            if (repository.findByTransactionRefNo(transactionRefNo).isPresent()) {
                log.info("Duplicate entry found for the transaction reference no {}", transactionRefNo);
                return buildErrorMessage("TC-409", "Duplicate entry found for the transaction reference no");
            }
            return saveTransferDetails(limitRequestDto, transactionRefNo);
        } catch (Exception e) {
            log.error("Error occurred while saving transfer details", e);
            return buildErrorMessage("TC-500", "Error occurred while saving transfer details");
        }
    }

    private TransferLimitResponseDto buildErrorMessage(String errorCode, String errorMessage) {
        return TransferLimitResponseDto.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    public TransferLimitResponseDto saveTransferDetails(TransferLimitRequestDto limitDto, String transactionRefNo) {
        log.info("Storing transferred/configured amount {} for beneficiary {}", htmlEscape(limitDto.getAmount()),
                htmlEscape(limitDto.getBeneficiaryId()));
        repository.save(limitDto.toEntity(transactionRefNo));
        return TransferLimitResponseDto.builder()
                .success(true)
                .build();
    }
}