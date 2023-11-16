package com.mashreq.transfercoreservice.fundtransfer.strategy;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.WITHIN_MASHREQ_FT;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import com.mashreq.transfercoreservice.config.EscrowConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.common.CommonUtils;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ContractProjectDetails;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.repository.EscrowAccountRepository;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.AccountNumberResolver;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountFreezeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.MinTransactionAmountValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.SameAccountValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.EscrowAccountDetails;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shahbazkh
 * @date 3/18/20
 */

@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class WithinMashreqStrategy implements FundTransferStrategy {

    private static final String INTERNAL_ACCOUNT_FLAG = "N";
    public static final String WITHIN_MASHREQ_TRANSACTION_CODE = "096";

    public static final String TRANSFER_AMOUNT_FOR_MIN_VALIDATION = "transfer-amount-for-min-validation";


    private final SameAccountValidator sameAccountValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final CurrencyValidator currencyValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;
    private final MaintenanceService maintenanceService;
    private final FundTransferMWService fundTransferMWService;
    private final BalanceValidator balanceValidator;
    private final DealValidator dealValidator;
    private final AsyncUserEventPublisher auditEventPublisher;
    private final NotificationService notificationService;
    private final AccountFreezeValidator freezeValidator;
    private final AccountNumberResolver accountNumberResolver;
    private final PostTransactionService postTransactionService;
    private final CCTransactionEligibilityValidator ccTrxValidator;
    private final EscrowAccountRepository escrowAccountRepository;

    private final EscrowConfig escrowConfig;

    private final MinTransactionAmountValidator minTransactionAmountValidator;

    @Value("${app.local.currency}")
    private String localCurrency;
    protected static final String MASHREQ = "Mashreq";
    protected static final String EMPTY_STRING = "";
    
    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {

        Instant start = Instant.now();
        responseHandler(ccTrxValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));
        Optional<AccountDetailsDTO> fromAccountOpt = accountsFromCore.stream()
                .filter(x -> request.getFromAccount().equals(x.getNumber()))
                .findFirst();

        //from account will always be present as it has been validated in the accountBelongsToCifValidator
        validationContext.add("from-account", fromAccountOpt.get());

        BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(metadata.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), request.getJourneyVersion(), metadata);
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));
        /** validating account freeze conditions */
        validateAccountFreezeDetails(request, metadata, validationContext);
        responseHandler(currencyValidator.validate(request, metadata, validationContext));

        CurrencyConversionDto currencyConversionDto = getAmountInSrcCurrency(request, fromAccountOpt.get());

        validationContext.add("transfer-amount-in-source-currency", currencyConversionDto.getAccountCurrencyAmount());
        validateAccountBalance(request, metadata, validationContext);
        
      //EscrowAccount
        if (escrowConfig.isEnabled()) {
            Optional<EscrowAccountDetails> escrowAccounts = escrowAccountRepository.findByAccountNo(beneficiaryDto.getAccountNumber());
            escrowAccounts.ifPresent(escrowAccountDetails -> request.setContractProjectDetails(buildEscrowAccountDetails(beneficiaryDto, escrowAccountDetails)));
        }

        //Limit Validation
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final BigDecimal limitUsageAmount = getLimitUsageAmount(fromAccountOpt.get(),currencyConversionDto.getAccountCurrencyAmount());

        validationContext.add(TRANSFER_AMOUNT_FOR_MIN_VALIDATION, limitUsageAmount);
        responseHandler(minTransactionAmountValidator.validate(request,metadata,validationContext));

        final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        String txnRefNo = validationResult.getTransactionRefNo();
        //Deal Validator
        log.info("Deal Validation Started");
		if (StringUtils.isNotBlank(request.getDealNumber()) && !request.getDealNumber().isEmpty()) {
			String trxCurrency = StringUtils.isBlank(request.getTxnCurrency()) ? localCurrency
					: request.getTxnCurrency();
			if (StringUtils.equalsIgnoreCase(trxCurrency, request.getCurrency())) {
				auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
						CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.toString(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage());
				GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY,
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage());
			}
			request.setTxnCurrency(trxCurrency);
            responseHandler(dealValidator.validate(request, metadata, validationContext));
   		 }

        log.info("Total time taken for {} strategy {} milli seconds ", htmlEscape(request.getServiceType()), htmlEscape(Long.toString(between(start, now()).toMillis())));

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountOpt.get(), beneficiaryDto, validationResult, currencyConversionDto);
        final FundTransferResponse fundTransferResponse = processTransaction(metadata, txnRefNo, fundTransferRequest,request);
        handleSuccessfulTransaction(request, metadata, userDTO, validationResult, fundTransferRequest,
				fundTransferResponse, beneficiaryDto);
        
        return prepareResponse(currencyConversionDto.getAccountCurrencyAmount(), limitUsageAmount, validationResult, txnRefNo, fundTransferResponse);

    }

	protected FundTransferResponse prepareResponse(final BigDecimal transferAmountInSrcCurrency,
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult, String txnRefNo,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .debitAmount(transferAmountInSrcCurrency)
                .transactionRefNo(txnRefNo).build();
	}

	protected FundTransferResponse processTransaction(RequestMetaData metadata, String txnRefNo,
			final FundTransferRequest fundTransferRequest,FundTransferRequestDTO request) {
		final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata, txnRefNo);
		return fundTransferResponse;
	}

	protected void handleSuccessfulTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
                                               UserDTO userDTO, final LimitValidatorResponse validationResult,
                                               final FundTransferRequest fundTransferRequest, final FundTransferResponse fundTransferResponse, BeneficiaryDto beneficiaryDto) {
		if(isSuccessOrProcessing(fundTransferResponse)) {
        	final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request.getTxnCurrency(),
                    request.getAmount(),fundTransferRequest.getBeneficiaryFullName(), fundTransferRequest.getToAccount());
            notificationService.sendNotifications(customerNotification, WITHIN_MASHREQ_FT, metadata, userDTO);
            fundTransferRequest.setTransferType(MASHREQ);
            fundTransferRequest.setNotificationType(NotificationType.LOCAL);
            fundTransferRequest.setStatus(MwResponseStatus.S.getName());
            /**added this here to avoid the impact; In some cases, amount is not updating while generating request
             * this is done to enable transactions with TXN currency as  */
            fundTransferRequest.setAmount(request.getAmount());
            fundTransferRequest.setTxnCurrency(request.getTxnCurrency());
            postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest, request, ofNullable(beneficiaryDto));
        }
	}

	protected void validateAccountBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validationContext) {
		responseHandler(balanceValidator.validate(request, metadata, validationContext));
	}

    private boolean isCurrencySame(FundTransferRequestDTO request) {
        return request.getCurrency().equalsIgnoreCase(request.getTxnCurrency());
    }
    
    /**
	 * Validates debit and credit freeze details for source and destination account details respectively
	 * @param request
	 * @param metadata
	 * @param validateAccountContext
	 */
	private void validateAccountFreezeDetails(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validateAccountContext) {
		SearchAccountDto toAccountDetails = accountService.getAccountDetailsFromCore(accountNumberResolver.generateAccountNumber(request.getToAccount()));
        validateAccountContext.add("credit-account-details", toAccountDetails);
        validateAccountContext.add("validate-credit-freeze", Boolean.TRUE);
        SearchAccountDto fromAccountDetails = accountService.getAccountDetailsFromCore(request.getFromAccount());
        validateAccountContext.add("debit-account-details", fromAccountDetails);
        validateAccountContext.add("validate-debit-freeze", Boolean.TRUE);
        freezeValidator.validate(request, metadata,validateAccountContext);
        request.setDestinationAccountCurrency(toAccountDetails.getCurrency());
	}
    
    protected CustomerNotification populateCustomerNotification(String transactionRefNo, String currency, BigDecimal amount, String beneficiaryName, String creditAccount) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(currency);
        customerNotification.setTxnRef(transactionRefNo);
        customerNotification.setBeneficiaryName(beneficiaryName);
        customerNotification.setCreditAccount(creditAccount);
        return customerNotification;
    }

    private CurrencyConversionDto getAmountInSrcCurrency(FundTransferRequestDTO request, AccountDetailsDTO sourceAccountDetailsDTO) {
        if(isCurrencySame(request)){
            CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
            currencyConversionDto.setExchangeRate(BigDecimal.ONE);
            currencyConversionDto.setAccountCurrencyAmount(request.getAmount());
            return currencyConversionDto;
        }
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency(request.getTxnCurrency());
        currencyRequest.setDealNumber(request.getDealNumber());
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto currencyConversionDto =  maintenanceService.convertBetweenCurrencies(currencyRequest);
        currencyConversionDto.setExchangeRateDisplayTxt(CommonUtils.generateDisplayString(currencyConversionDto, currencyRequest));
        return currencyConversionDto;
    }


    private BigDecimal getLimitUsageAmount(final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        return localCurrency.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertAmountInLocalCurrency(final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setTransactionCurrency(localCurrency);
        return maintenanceService.convertCurrency(currencyConversionRequestDto).getTransactionAmount();
    }
    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, BeneficiaryDto beneficiaryDto,
                                                                  LimitValidatorResponse validationResult, CurrencyConversionDto currencyConversionDto) {
    	FundTransferRequest trnsrequest = 
         FundTransferRequest.builder()
                 .productId("AACT")
                .amount(!sourceAccount.getCurrency().equals(request.getTxnCurrency()) ? request.getAmount() : null)
                .srcCcyAmt(sourceAccount.getCurrency().equals(request.getTxnCurrency()) ? request.getAmount() : currencyConversionDto.getAccountCurrencyAmount())
                .srcAmount(sourceAccount.getCurrency().equals(request.getTxnCurrency()) ? request.getAmount() : null)
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(beneficiaryDto.getAccountNumber())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(sourceAccount.getCurrency())
                .sourceBranchCode(sourceAccount.getBranchCode())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                /** added if condition to avoid impact on existing logic, as per actual logic , destination currency should not be null*/
                .destinationCurrency(StringUtils.defaultIfBlank(request.getDestinationAccountCurrency(), request.getTxnCurrency()))
                .txnCurrency(request.getTxnCurrency())
                .transactionCode(WITHIN_MASHREQ_TRANSACTION_CODE)
                .internalAccFlag(INTERNAL_ACCOUNT_FLAG)
                .dealNumber(request.getDealNumber())
                .dealRate(request.getDealRate())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .paymentNote(request.getPaymentNote())
                .accountClass(sourceAccount.getAccountType())
                .serviceType(request.getServiceType())
                .exchangeRateDisplayTxt(currencyConversionDto.getExchangeRateDisplayTxt())
                .contractProjectDetails(request.getContractProjectDetails())
                .build();
    	return trnsrequest;
    }

    
    private boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }

    private boolean isTrustAccount(List<String> trustAccounts, SearchAccountDto searchAccountDto){
        return trustAccounts.stream().anyMatch(trust -> trust.contains(searchAccountDto.getAccountType().getAccountType()));
    }

    private boolean isOaAccount(List<String> oaAccounts, SearchAccountDto searchAccountDto){
        return oaAccounts.stream().anyMatch(oa -> oa.contains(searchAccountDto.getAccountType().getAccountType()));
    }


    private ContractProjectDetails buildEscrowAccountDetails(BeneficiaryDto beneficiaryDto, EscrowAccountDetails escrowAccounts) {
        log.info("Bene Escrow Account is available in DB");
        SearchAccountDto searchAccountDto = accountService.getAccountDetailsFromCore(beneficiaryDto.getAccountNumber());
        if (isTrustAccount(escrowConfig.getTrustAccounts(), searchAccountDto)) {
            log.info("Building Trust Account Details");
            return buildContractProjectDetails(true, escrowAccounts);
        } else if (isOaAccount(escrowConfig.getOaAccounts(), searchAccountDto)) {
            log.info("Populate OA Account Details");
            return buildContractProjectDetails(false, escrowAccounts);
        }
        return null;
    }

        private ContractProjectDetails buildContractProjectDetails(boolean isTrust, EscrowAccountDetails escrowAccounts) {
            ContractProjectDetails contractProjectDetails = new ContractProjectDetails();
            contractProjectDetails.setModule("FT");
            contractProjectDetails.setDepositTfrNo(EMPTY_STRING);
            if (isTrust) {
                log.info("Populate Trust Account Details");
                contractProjectDetails.setProjectName(escrowAccounts.getProjectName());
                contractProjectDetails.setUnitPayment("Y");
                contractProjectDetails.setUnitId(escrowAccounts.getProjectNo());
            } else {
                log.info("Populate OA Account Details");
                contractProjectDetails.setProjectName(StringUtils.isNotBlank(escrowAccounts.getProjectName()) ? escrowAccounts.getProjectName() : escrowConfig.getDefaultProjectName());
                contractProjectDetails.setUnitPayment("N");
                contractProjectDetails.setUnitId(EMPTY_STRING);

            }
            return contractProjectDetails;
        }
}
