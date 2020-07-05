package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.event.publisher.Publisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.event.model.EventType.FIN_TRANSACTION_VALIDATION;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class FinTxnNoValidator implements Validator {

    private final PaymentHistoryService paymentHistoryService;
    private final Publisher auditEventPublisher;

    @Override
    public ValidationResult validate(final FundTransferRequestDTO request, final RequestMetaData metadata, final ValidationContext context) {
        log.info("Validating fin-txn-no {} for service type [ {} ] ", request.getFinTxnNo(), request.getServiceType());

        if (paymentHistoryService.isFinancialTransactionPresent(request.getFinTxnNo())) {
            log.warn("Duplicate fin-txn-no {} found for service type [ {} ] ", request.getFinTxnNo(), request.getServiceType());
            auditEventPublisher.publishEvent(FIN_TRANSACTION_VALIDATION, EventStatus.FAILURE, metadata, null);
            return ValidationResult.builder()
                    .success(false)
                    .transferErrorCode(TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST)
                    .build();
        }
        log.info("Financial Txn No Validating successful service type [ {} ] ", request.getServiceType());
        auditEventPublisher.publishEvent(FIN_TRANSACTION_VALIDATION, EventStatus.SUCCESS, metadata, null);
        return ValidationResult.builder().success(true).build();
    }
}
