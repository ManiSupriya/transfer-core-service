package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NOT_BELONG_TO_CIF;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_BELONGS_TO_CIF;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

/**
 * @author Thanigachalamp
 *
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CCBelongsToCifValidator implements Validator {

    private final AsyncUserEventPublisher auditEventPublisher;
    private final EncryptionService encryptionService = new EncryptionService();

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating CC Account for service type [ {} ] ", htmlEscape(request.getServiceType()));

        final List<CardDetailsDTO> accounts = context.get("account-details", List.class);
        final Boolean validateToAccount = context.get("validate-to-account", Boolean.class);

        // TODO Need to confirm with Soma
        /*if (validateToAccount != null && validateToAccount && !isCCBelongsToCif(accounts, request.getToAccount())) {
            auditEventPublisher.publishFailureEvent(ACCOUNT_BELONGS_TO_CIF, metadata, null,
                    ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), null);
            return prepareValidationResult(Boolean.FALSE);
        }*/

        final Boolean validateFromAccount = context.get("validate-from-account", Boolean.class);

        if (validateFromAccount != null && validateFromAccount && !isCCBelongsToCif(accounts, request.getCardNo())) {
            auditEventPublisher.publishFailureEvent(ACCOUNT_BELONGS_TO_CIF, metadata, null,
                    ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), ACCOUNT_NOT_BELONG_TO_CIF.getCustomErrorCode(), null);
            return prepareValidationResult(Boolean.FALSE);
        }


        log.info("CC Account validation Successful for service type [ {} ] ", htmlEscape(request.getServiceType()));
        auditEventPublisher.publishSuccessEvent(ACCOUNT_BELONGS_TO_CIF, metadata, null);
        return prepareValidationResult(Boolean.TRUE);
    }

    private boolean isCCBelongsToCif(List<CardDetailsDTO> coreAccounts, String encryptedCardNo) {
        boolean isMatch = false;
        String decryptedCardNo;
        String givenDecryptedCardNo = encryptionService.decrypt(encryptedCardNo);
        for(CardDetailsDTO currCardDetails : coreAccounts){
            decryptedCardNo = encryptionService.decrypt(currCardDetails.getEncryptedCardNumber());
            if(decryptedCardNo.equalsIgnoreCase(givenDecryptedCardNo)){
                isMatch = true;
                break;
            }
        }
        return isMatch;
    }

    private ValidationResult prepareValidationResult(boolean status) {
        return status ? ValidationResult.builder().success(true).build() : ValidationResult.builder()
                .success(false)
                .transferErrorCode(ACCOUNT_NOT_BELONG_TO_CIF)
                .build();
    }

}
