package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BALANCE_NOT_SUFFICIENT;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

/**
 * @author shahbazkh
 * @date 3/26/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceValidator implements Validator<FundTransferRequestDTO> {


    private final AsyncUserEventPublisher auditEventPublisher;
    private final AccountService accountService;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating Balance for service type [ {} ] ", htmlEscape(request.getServiceType()));
        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);
        log.info("Balance in account [ {} {} ] ", htmlEscape(fromAccount.getAvailableBalance()), htmlEscape(fromAccount.getCurrency()));

        BigDecimal transferAmountInSrcCurrency = context.get("transfer-amount-in-source-currency", BigDecimal.class);
        log.info("Amount to be credited is [ {} {} ] ", htmlEscape(transferAmountInSrcCurrency), htmlEscape(fromAccount.getCurrency()));

        if (!isBalanceAvailable(fromAccount.getAvailableBalance(), transferAmountInSrcCurrency)) {
            auditEventPublisher.publishFailureEvent(FundTransferEventType.BALANCE_VALIDATION, metadata, null,
                    BALANCE_NOT_SUFFICIENT.getErrorMessage(),BALANCE_NOT_SUFFICIENT.getCustomErrorCode(), null);
            return ValidationResult.builder().success(false).transferErrorCode(BALANCE_NOT_SUFFICIENT)
                    .build();
        }
        log.info("Balance Validation successful");
        auditEventPublisher.publishSuccessEvent(FundTransferEventType.BALANCE_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }
    
    public boolean validateBalance(CardLessCashGenerationRequest cardLessCashGenerationRequest, RequestMetaData metaData) {

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());

        final ValidationContext validateAccountContext = new ValidationContext();
        validateAccountContext.add("account-details", accountsFromCore);
        validateAccountContext.add("validate-from-account", Boolean.TRUE);

        final AccountDetailsDTO fromAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore, cardLessCashGenerationRequest.getAccountNo());

        return validate(cardLessCashGenerationRequest, metaData, fromAccount);
    }

    private boolean isBalanceAvailable(BigDecimal availableBalance, BigDecimal paidAmount) {
        return availableBalance.compareTo(paidAmount) >= 0;
    }
    
    AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }
    
    public boolean validate(CardLessCashGenerationRequest cardLessCashGenerationRequest, RequestMetaData metadata, AccountDetailsDTO fromAccount) {

        log.info("Balance in account [ {} {} ] ", htmlEscape(fromAccount.getAvailableBalance()), htmlEscape(fromAccount.getCurrency()));
        BigDecimal amountTobePaid = cardLessCashGenerationRequest.getAmount();
        log.info("Amount to be credited is [ {} {} ] ", htmlEscape(amountTobePaid), htmlEscape(fromAccount.getCurrency()));
        return isBalanceAvailable(fromAccount.getAvailableBalance(), amountTobePaid);
    }
}
