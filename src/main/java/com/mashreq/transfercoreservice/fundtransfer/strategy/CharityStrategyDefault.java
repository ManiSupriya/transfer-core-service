package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.common.NotificationName;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

/**
 * @author shahbazkh
 * @date 3/18/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CharityStrategyDefault implements FundTransferStrategy {

    private static final String INTERNAL_ACCOUNT_FLAG = "N";

    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final AccountService accountService;
    private final BeneficiaryClient beneficiaryClient;
    private final CharityValidator charityValidator;
    private final CurrencyValidator currencyValidator;
    private final DealValidator dealValidator;
    private final LimitValidator limitValidator;
    private final FundTransferMWService fundTransferMWService;
    private final BalanceValidator balanceValidator;
    private final NotificationService notificationService;



    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {

        Instant start = Instant.now();


        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));


        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validateContext = new ValidationContext();
        validateContext.add("account-details", accountsFromCore);
        validateContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateContext));


        Optional<AccountDetailsDTO> fromAccountOpt = accountsFromCore.stream()
                .filter(x -> request.getFromAccount().equals(x.getNumber()))
                .findFirst();

        //from account will always be present as it has been validated in the accountBelongsToCifValidator
        validateContext.add("from-account", fromAccountOpt.get());

        CharityBeneficiaryDto charityBeneficiaryDto = beneficiaryClient.getCharity(request.getBeneficiaryId()).getData();
        validateContext.add("charity-beneficiary-dto", charityBeneficiaryDto);
        validateContext.add("to-account-currency", charityBeneficiaryDto.getCurrencyCode());
        responseHandler(charityValidator.validate(request, metadata, validateContext));
        responseHandler(currencyValidator.validate(request, metadata, validateContext));

        //TODO
        
        //Deal Validator
        log.info("Deal Validation Started");
        if (StringUtils.isNotBlank(request.getDealNumber()) && !request.getDealNumber().isEmpty()) {
            responseHandler(dealValidator.validate(request, metadata, validateContext));
   		 }

        //Balance Validation
        validateContext.add("transfer-amount-in-source-currency", request.getAmount());
        responseHandler(balanceValidator.validate(request, metadata, validateContext));



        //responseHandler(balanceValidator.validate(request, metadata,validateContext));

        // Assuming to account is always in AED
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;

        log.info("Limit Validation start.");
        BigDecimal limitUsageAmount = request.getAmount();
        final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        log.info("Limit Validation successful");
        String txnRefNo = validationResult.getTransactionRefNo();

       final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountOpt.get(), charityBeneficiaryDto, validationResult);
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata, txnRefNo);
		if (isSuccessOrProcessing(fundTransferResponse)) {
			final CustomerNotification customerNotification = populateCustomerNotification(
					validationResult.getTransactionRefNo(), request.getCurrency(), request.getAmount());
			notificationService.sendNotifications(customerNotification, OTHER_ACCOUNT_TRANSACTION, metadata, userDTO, NotificationName.CHARITY_TRANSACTION);
		}

        //final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);

        log.info("Total time taken for {} strategy {} milli seconds ", htmlEscape(request.getServiceType()), htmlEscape(Long.toString(between(start, now()).toMillis())));

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .transactionRefNo(txnRefNo).build();

    }
    
    private CustomerNotification populateCustomerNotification(String transactionRefNo, String currency, BigDecimal amount) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(currency);
        customerNotification.setTxnRef(transactionRefNo);
        return customerNotification;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, CharityBeneficiaryDto charityBeneficiaryDto, LimitValidatorResponse validationResult) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(charityBeneficiaryDto.getAccountNumber())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(sourceAccount.getCurrency())
                .sourceBranchCode(sourceAccount.getBranchCode())
                .beneficiaryFullName(charityBeneficiaryDto.getName())
                .destinationCurrency(charityBeneficiaryDto.getCurrencyCode())
                .transactionCode("096")
                .internalAccFlag(INTERNAL_ACCOUNT_FLAG)
                .dealNumber(request.getDealNumber())
                .dealRate(request.getDealRate())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .build();

    }
    
    private boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }
}
