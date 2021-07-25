package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.FT_CC_BALANCE_NOT_SUFFICIENT;

/**
 * @author Thanigachalam P
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CCBalanceValidator implements Validator<FundTransferEligibiltyRequestDTO> {


    private final AsyncUserEventPublisher auditEventPublisher;
    private final AccountService accountService;

    @Override
    public ValidationResult validate(FundTransferEligibiltyRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("CC Validating Balance for service type [ {} ] ", htmlEscape(request.getServiceType()));
        CardDetailsDTO fromAccount = context.get("from-account", CardDetailsDTO.class);
        log.info("Balance in account [ {} ] ", htmlEscape(fromAccount.getAvailableCreditLimit()));

        BigDecimal transferAmountInSrcCurrency = context.get("transfer-amount-in-source-currency", BigDecimal.class);
        log.info("Amount to be credited is [ {} ] ", htmlEscape(transferAmountInSrcCurrency));

        if (!isBalanceAvailable(fromAccount.getAvailableCreditLimit(), transferAmountInSrcCurrency)) {
            auditEventPublisher.publishFailureEvent(FundTransferEventType.BALANCE_VALIDATION, metadata, null,
                    FT_CC_BALANCE_NOT_SUFFICIENT.getErrorMessage(),FT_CC_BALANCE_NOT_SUFFICIENT.getCustomErrorCode(), null);
            return ValidationResult.builder().success(false).transferErrorCode(FT_CC_BALANCE_NOT_SUFFICIENT)
                    .build();
        }
        log.info("Balance Validation successful");
        auditEventPublisher.publishSuccessEvent(FundTransferEventType.BALANCE_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }
    
    private boolean isBalanceAvailable(BigDecimal availableBalance, BigDecimal paidAmount) {
        return availableBalance.compareTo(paidAmount) >= 0;
    }

}
