package com.mashreq.transfercoreservice.fundtransfer.strategy;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.XAU;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.OWN_ACCOUNT_FT;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.Optional.empty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.common.CommonUtils;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.service.DigitalUserSegment;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
@Slf4j
@Service("ownAccountStrategy")
@Getter
@RequiredArgsConstructor
public class OwnAccountStrategy implements FundTransferStrategy {

    private static final String INTERNAL_ACCOUNT_FLAG = "N";
    public static final String OWN_ACCOUNT_TRANSACTION_CODE = "096";
    private static final String  OWN_ACCOUNT_TRANSACTION = "OWN_ACCOUNT_TRANSACTION";
    public static final String OWN_ACCOUNT = "Own Account";
    private static final String MB_META = "MBMETA";
    private static final String MB_META_PROD_ID = "MT5I";
    public static final String TRANSFER_AMOUNT_FOR_MIN_VALIDATION = "transfer-amount-for-min-validation";
    private static final String WYMA_FUND_TRANSFER_POP = "Own Account Transfer";

    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final SameAccountValidator sameAccountValidator;
    private final CurrencyValidator currencyValidator;
    private final LimitValidator limitValidator;
    private final AccountService accountService;
    private final DealValidator dealValidator;
    private final MaintenanceService maintenanceService;
    private final FundTransferMWService fundTransferMWService;
    private final BalanceValidator balanceValidator;
    private final NotificationService notificationService;
    private final AsyncUserEventPublisher auditEventPublisher;
    private final DigitalUserSegment digitalUserSegment;
    private final AccountFreezeValidator freezeValidator;
    private final PostTransactionService postTransactionService;
    private final MinTransactionAmountValidator minTransactionAmountValidator;


    @Value("${app.local.currency}")
    private String localCurrency;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {

        setPurposeOfPayment(request);

        Instant start = Instant.now();

        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validateAccountContext = new ValidationContext();
        validateAccountContext.add("account-details", accountsFromCore);
        validateAccountContext.add("validate-to-account", Boolean.TRUE);
        validateAccountContext.add("validate-from-account", Boolean.TRUE);

        validateAccountFreezeDetails(request, metadata, validateAccountContext);

        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateAccountContext));

        final AccountDetailsDTO toAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getToAccount());
        final AccountDetailsDTO fromAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());

        if(StringUtils.isNotBlank(request.getTxnCurrency()) && !request.getTxnCurrency().equalsIgnoreCase(toAccount.getCurrency())  && !request.getTxnCurrency().equalsIgnoreCase(fromAccount.getCurrency())){
            auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata,
                    CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
                    TransferErrorCode.TXN_CURRENCY_INVALID.toString(),
                    TransferErrorCode.TXN_CURRENCY_INVALID.getErrorMessage(),
                    TransferErrorCode.TXN_CURRENCY_INVALID.getErrorMessage());
            GenericExceptionHandler.handleError(TransferErrorCode.TXN_CURRENCY_INVALID,
                    TransferErrorCode.TXN_CURRENCY_INVALID.getErrorMessage(),
                    TransferErrorCode.TXN_CURRENCY_INVALID.getErrorMessage());
        }

        validateAccountContext.add("from-account", fromAccount);
        validateAccountContext.add("to-account", toAccount);
        if (request.getSrcAmount() != null && StringUtils.isNotBlank(request.getSrcAmount().toString()))
            validateAccountContext.add("to-account-currency", toAccount.getCurrency());
        else
            validateAccountContext.add("to-account-currency", request.getTxnCurrency());
        responseHandler(currencyValidator.validate(request, metadata, validateAccountContext));

        BigDecimal transactionAmount = request.getAmount() == null ? request.getSrcAmount() : request.getAmount();

        //added this condition for sell gold since we have amount in srcCurrency
        CurrencyConversionDto conversionResult = request.getTxnCurrency() != null && request.getAmount() != null && !isCurrencySame(request.getTxnCurrency(), fromAccount.getCurrency())
                ? getCurrencyExchangeObject(transactionAmount, request, toAccount, fromAccount) :
                getExchangeObjectForSrcAmount(transactionAmount, toAccount, fromAccount);

        final BigDecimal transferAmountInSrcCurrency = request.getTxnCurrency() != null && request.getAmount() != null && !isCurrencySame(request.getTxnCurrency(), fromAccount.getCurrency())
                ? conversionResult.getAccountCurrencyAmount()
                : transactionAmount;

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
			responseHandler(dealValidator.validate(request, metadata, validateAccountContext));
		}

        validateAccountBalance(request, metadata, validateAccountContext, transferAmountInSrcCurrency);


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccount,transferAmountInSrcCurrency);
        request.setServiceType(getBeneficiaryCode(request));

        if(goldSilverTransfer(request)){
            limitValidator.validateMin(userDTO, request.getServiceType(), transactionAmount, metadata);
        }
        else {
            validateAccountContext.add(TRANSFER_AMOUNT_FOR_MIN_VALIDATION, limitUsageAmount);
            responseHandler(minTransactionAmountValidator.validate(request, metadata, validateAccountContext));
        }

        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final LimitValidatorResponse validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccount, toAccount,validationResult, conversionResult);

        fundTransferRequest.setProductId(isMT5AccountProdID(fundTransferRequest));
        final FundTransferResponse fundTransferResponse = processTransfer(metadata, validationResult, fundTransferRequest,request);

        handleSuccessfulTransaction(request, metadata, userDTO, transactionAmount, validationResult, fundTransferResponse, fundTransferRequest);

        log.info("Total time taken for {} strategy {} milli seconds ", htmlEscape(request.getServiceType()), htmlEscape(Long.toString(between(start, now()).toMillis())));
        prepareAndCallPostTransactionActivity(metadata,fundTransferRequest,request,fundTransferResponse,conversionResult);
        return prepareResponse(transferAmountInSrcCurrency, limitUsageAmount, validationResult, fundTransferResponse);
    }

    protected FundTransferResponse prepareResponse(final BigDecimal transferAmountInSrcCurrency,
			final BigDecimal limitUsageAmount, final LimitValidatorResponse validationResult,
			final FundTransferResponse fundTransferResponse) {
		return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .transactionRefNo(validationResult.getTransactionRefNo())
                .debitAmount(transferAmountInSrcCurrency)
                .build();
	}

	protected void handleSuccessfulTransaction(FundTransferRequestDTO request, RequestMetaData metadata,
			UserDTO userDTO, BigDecimal transactionAmount, final LimitValidatorResponse validationResult,
			final FundTransferResponse fundTransferResponse, final FundTransferRequest fundTransferRequest) {
		if(isSuccessOrProcessing(fundTransferResponse)){
		   final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request,transactionAmount,metadata,fundTransferRequest.getBeneficiaryFullName(),fundTransferRequest.getToAccount());
		   notificationService.sendNotifications(customerNotification, OWN_ACCOUNT_FT, metadata, userDTO);
		   }
	}

	protected FundTransferResponse processTransfer(RequestMetaData metadata,
			final LimitValidatorResponse validationResult, final FundTransferRequest fundTransferRequest,
			FundTransferRequestDTO request) {
		return fundTransferMWService.transfer(fundTransferRequest, metadata,validationResult.getTransactionRefNo());
	}

	protected void validateAccountBalance(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validateAccountContext, final BigDecimal transferAmountInSrcCurrency) {
		validateAccountContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validateAccountContext));
	}

	/**
	 * Validates debit and credit freeze details for source and destination account details respectively
	 * @param request
	 * @param metadata
	 * @param validateAccountContext
	 */
	private void validateAccountFreezeDetails(FundTransferRequestDTO request, RequestMetaData metadata,
			final ValidationContext validateAccountContext) {
		SearchAccountDto toAccountDetails = accountService.getAccountDetailsFromCore(request.getToAccount());
        validateAccountContext.add("credit-account-details", toAccountDetails);
        validateAccountContext.add("validate-credit-freeze", Boolean.TRUE);
        SearchAccountDto fromAccountDetails = accountService.getAccountDetailsFromCore(request.getFromAccount());
        validateAccountContext.add("debit-account-details", fromAccountDetails);
        validateAccountContext.add("validate-debit-freeze", Boolean.TRUE);
        freezeValidator.validate(request, metadata,validateAccountContext);
	}

	protected void prepareAndCallPostTransactionActivity(RequestMetaData metadata, FundTransferRequest fundTransferRequest,
                                                         FundTransferRequestDTO request, FundTransferResponse fundTransferResponse,
                                                         CurrencyConversionDto conversionResult) {
        boolean isSuccess = MwResponseStatus.S.equals(fundTransferResponse.getResponseDto().getMwResponseStatus());
        if(goldSilverTransfer(request) && isSuccess){
            if(buyGoldOrSilverRequest(fundTransferRequest)){
                fundTransferRequest.setNotificationType(NotificationType.GOLD_SILVER_BUY_SUCCESS);
                fundTransferRequest.setSrcAmount(conversionResult.getAccountCurrencyAmount());
                fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getDestinationCurrency()));
            }
            else{
                fundTransferRequest.setNotificationType(NotificationType.GOLD_SILVER_SELL_SUCCESS);
                fundTransferRequest.setAmount(conversionResult.getTransactionAmount());
                fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getSourceCurrency()));
            }
            postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest, request, empty());
        }
        else if(isSuccess){
            fundTransferRequest.setTransferType(OWN_ACCOUNT);
            fundTransferRequest.setNotificationType(NotificationType.LOCAL);
            fundTransferRequest.setStatus(MwResponseStatus.S.getName());
            postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest, request, empty());
        }
    }

    protected CustomerNotification populateCustomerNotification(String transactionRefNo, FundTransferRequestDTO requestDTO, BigDecimal amount, RequestMetaData metadata, String beneficiaryName, String creditAccount) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(requestDTO.getTxnCurrency());
        if(StringUtils.isEmpty(requestDTO.getTxnCurrency())) {
            customerNotification.setCurrency(requestDTO.getCurrency());
        }
        customerNotification.setTxnRef(transactionRefNo);
        customerNotification.setSegment(digitalUserSegment.getCustomerCareInfo(metadata.getSegment()));
        customerNotification.setBeneficiaryName(beneficiaryName);
        customerNotification.setCreditAccount(creditAccount);
        return customerNotification;
    }

    private String getBeneficiaryCode(FundTransferRequestDTO request) {
        if(goldSilverTransfer(request)){
            return request.getTxnCurrency();
        }
        else return request.getServiceType();
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, AccountDetailsDTO destinationAccount,
                                                                  LimitValidatorResponse validationResult, CurrencyConversionDto currencyConversionDto) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
                .srcCcyAmt(sourceAccount.getCurrency().equals(request.getTxnCurrency()) ? request.getAmount() : currencyConversionDto.getAccountCurrencyAmount())
                .srcAmount(request.getSrcAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(destinationAccount.getNumber())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(sourceAccount.getCurrency())
                .sourceBranchCode(sourceAccount.getBranchCode())
                .beneficiaryFullName(destinationAccount.getCustomerName())
                .destinationCurrency(destinationAccount.getCurrency())
                .transactionCode(OWN_ACCOUNT_TRANSACTION_CODE)
                .internalAccFlag(INTERNAL_ACCOUNT_FLAG)
                .dealNumber(request.getDealNumber())
                .dealRate(request.getDealRate())
                .txnCurrency(request.getTxnCurrency())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .paymentNote(request.getPaymentNote())
                .accountClass(sourceAccount.getAccountType())
                .serviceType(request.getServiceType())
                .exchangeRate(currencyConversionDto.getExchangeRate())
                .exchangeRateDisplayTxt(currencyConversionDto.getExchangeRateDisplayTxt())
                .build();

    }


    protected BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
        return localCurrency.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    protected BigDecimal convertAmountInLocalCurrency(final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setTransactionCurrency(localCurrency);

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private boolean isCurrencySame(String destinationCurrency, String sourceCurrency) {
        return destinationCurrency.equalsIgnoreCase(sourceCurrency);
    }

    private CurrencyConversionDto getCurrencyExchangeObject(BigDecimal transactionAmount, FundTransferRequestDTO request, AccountDetailsDTO destAccount, AccountDetailsDTO sourceAccount) {
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccount.getNumber());
        currencyRequest.setAccountCurrency(sourceAccount.getCurrency());
        currencyRequest.setTransactionCurrency(destAccount.getCurrency());
        currencyRequest.setDealNumber(request.getDealNumber());
        currencyRequest.setTransactionAmount(transactionAmount);
        CurrencyConversionDto currencyConversionDto =  maintenanceService.convertBetweenCurrencies(currencyRequest);
        currencyConversionDto.setExchangeRateDisplayTxt(CommonUtils.generateDisplayString(currencyConversionDto, currencyRequest));
        return currencyConversionDto;

    }

    private boolean goldSilverTransfer(FundTransferRequestDTO request){
        return (XAU.getName().equals(request.getTxnCurrency()) || ServiceType.XAG.getName().equals(request.getTxnCurrency()));
    }

    private boolean buyGoldOrSilverRequest(FundTransferRequest request){
        return (XAU.getName().equals(request.getDestinationCurrency()) || ServiceType.XAG.getName().equals(request.getDestinationCurrency()));
    }

    private CurrencyConversionDto getExchangeObjectForSrcAmount(BigDecimal transactionAmount, AccountDetailsDTO destAccount, AccountDetailsDTO sourceAccount) {
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccount.getNumber());
        currencyRequest.setAccountCurrency(sourceAccount.getCurrency());
        currencyRequest.setAccountCurrencyAmount(transactionAmount);
        currencyRequest.setTransactionCurrency(destAccount.getCurrency());
        return maintenanceService.convertBetweenCurrencies(currencyRequest);
    }

    private String getTransferType(String currency){
        if(XAU.getName().equals(currency)){
            return "Gold";
        }
        else if( ServiceType.XAG.getName().equals(currency)){
            return "Silver";
        }
        return null;
    }
    
    private boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }
    
    private String isMT5AccountProdID(FundTransferRequest request) {
   	 final CompletableFuture<SearchAccountDto> searchAccountFut = CompletableFuture.supplyAsync(() ->
        accountService.getAccountDetailsFromCore(request.getToAccount()));
   	 final SearchAccountDto searchAccountDto = searchAccountFut.join();
   	if(MB_META.equalsIgnoreCase(searchAccountDto.getAccountType().getAccountType())) {
   		return MB_META_PROD_ID;
   	}
   	return request.getProductId();
   }

    private void setPurposeOfPayment(FundTransferRequestDTO request) {
        request.setPurposeDesc(WYMA_FUND_TRANSFER_POP);
    }
}