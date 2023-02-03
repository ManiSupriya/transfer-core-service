package com.mashreq.transfercoreservice.fundtransfer.user;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalUserService {
    private final DigitalUserRepository digitalUserRepository;

    public DigitalUser getDigitalUser(RequestMetaData fundTransferMetadata) {
        Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(fundTransferMetadata.getPrimaryCif());
        if (digitalUserOptional.isPresent()) {
            log.info("Digital User found successfully {} ", digitalUserOptional.get());
            return digitalUserOptional.get();
        } else {
            GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
            return null;
        }
    }
}
