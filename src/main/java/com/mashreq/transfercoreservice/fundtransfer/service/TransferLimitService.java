package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.repository.TransferLimitRepository;
import com.mashreq.transfercoreservice.transactionqueue.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferLimitService {

    private final TransferLimitRepository repository;

    private final TransactionRepository transactionRepository;

    public TransferLimitResponseDto validateAndSaveTransferDetails(TransferLimitRequestDto limitRequestDto,
                                                                   String transactionRefNo) {
        try {
            if (!transactionRepository.existsPaymentHistoryByTransactionRefNo(transactionRefNo)) {
                return TransferLimitResponseDto.builder()
                        .success(false)
                        .errorMessage("Transaction not found")
                        .errorCode("TC-204")
                        .build();
            }
            return saveTransferDetails(limitRequestDto);
        } catch (Exception e) {
            log.error("Error occurred while saving transfer details", e);
            return TransferLimitResponseDto.builder()
                    .success(false)
                    .errorMessage("Error occurred while saving transfer details")
                    .errorCode("TC-500")
                    .build();
        }
    }

    public TransferLimitResponseDto saveTransferDetails(TransferLimitRequestDto limitDto) {
        log.info("Storing transferred/configured amount {} for beneficiary {}", htmlEscape(limitDto.getAmount()),
                htmlEscape(limitDto.getBeneficiaryId()));
        repository.save(limitDto.toEntity());
        return TransferLimitResponseDto.builder()
                .success(true)
                .build();
    }
}