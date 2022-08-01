package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitDto;
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

    public void saveTransferDetails(TransferLimitDto limitDto) {
        log.info("Storing transferred/configured amount {} for beneficiary {}", htmlEscape(limitDto.getAmount()),
                htmlEscape(limitDto.getBeneficiaryId()));
        repository.save(limitDto.toEntity());
    }
}