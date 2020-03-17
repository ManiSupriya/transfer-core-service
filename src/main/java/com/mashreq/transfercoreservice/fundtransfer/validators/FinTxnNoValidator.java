package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class FinTxnNoValidator implements Validator {

    private final PaymentHistoryService paymentHistoryService;

    @Override
    public ValidationResult validate(final FundTransferRequestDTO request, final FundTransferMetadata metadata, final ValidationContext context) {
        log.info("Validation fin-txn-no {} ", request.getFinTxnNo());
        if (paymentHistoryService.isFinancialTransactionPresent(request.getFinTxnNo()))
            return ValidationResult.builder()
                    .success(false)
                    .transferErrorCode(TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST)
                    .build();

        return ValidationResult.builder().success(true).build();
    }
}
