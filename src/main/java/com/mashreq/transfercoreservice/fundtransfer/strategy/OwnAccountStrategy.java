package com.mashreq.transfercoreservice.fundtransfer.strategy;

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
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.service.DigitalUserSegment;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OwnAccountStrategy implements FundTransferStrategy {

    private static final String INTERNAL_ACCOUNT_FLAG = "N";
    public static final String OWN_ACCOUNT_TRANSACTION_CODE = "096";
    public static final String LOCAL_CURRENCY = "AED";
    private static final String  OWN_ACCOUNT_TRANSACTION = "OWN_ACCOUNT_TRANSACTION";
    public static final String OWN_ACCOUNT = "Own Account";
    private static final String MB_META = "MBMETA";
    private static final String MB_META_PROD_ID = "MT5I";

    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
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

    @Autowired
    private PostTransactionService postTransactionService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {

        Instant start = Instant.now();

        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validateAccountContext = new ValidationContext();
        validateAccountContext.add("account-details", accountsFromCore);
        validateAccountContext.add("validate-to-account", Boolean.TRUE);
        validateAccountContext.add("validate-from-account", Boolean.TRUE);

        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateAccountContext));

        final AccountDetailsDTO toAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getToAccount());
        final AccountDetailsDTO fromAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());

        if(!request.getTxnCurrency().equalsIgnoreCase(toAccount.getCurrency())  && !request.getTxnCurrency().equalsIgnoreCase(fromAccount.getCurrency())){
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
        validateAccountContext.add("to-account-currency", toAccount.getCurrency());
        responseHandler(currencyValidator.validate(request, metadata, validateAccountContext));

        BigDecimal transactionAmount = request.getAmount() == null ? request.getSrcAmount() : request.getAmount();

        //added this condition for sell gold since we have amount in srcCurrency
        CurrencyConversionDto conversionResult = request.getAmount()!=null && !isCurrencySame(toAccount, fromAccount)
                    ? getCurrencyExchangeObject(transactionAmount,toAccount,fromAccount):
                    getExchangeObjectForSrcAmount(transactionAmount,toAccount,fromAccount);


        final BigDecimal transferAmountInSrcCurrency = request.getAmount()!=null && !isCurrencySame(toAccount, fromAccount)
            ? conversionResult.getAccountCurrencyAmount()
            : transactionAmount;
            
         //Deal Validator
        log.info("Deal Validation Started");
		if (StringUtils.isNotBlank(request.getDealNumber()) && !request.getDealNumber().isEmpty()) {
			String trxCurrency = StringUtils.isBlank(request.getTxnCurrency()) ? LOCAL_CURRENCY
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

        validateAccountContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validateAccountContext));


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccount,transferAmountInSrcCurrency);
        request.setServiceType(getBeneficiaryCode(request));
        if(goldSilverTransfer(request)){
            limitValidator.validateMin(userDTO, request.getServiceType(), transactionAmount, metadata);
        }
        Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
        final LimitValidatorResponse validationResult = limitValidator.validateWithProc(userDTO, request.getServiceType(), limitUsageAmount, metadata, bendId);
        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccount, toAccount,conversionResult.getExchangeRate(),validationResult);

        fundTransferRequest.setProductId(isMT5AccountProdID(fundTransferRequest));
       final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata,validationResult.getTransactionRefNo());

       if(isSuccessOrProcessing(fundTransferResponse)){
       final CustomerNotification customerNotification = populateCustomerNotification(validationResult.getTransactionRefNo(),request,transactionAmount,metadata);
       notificationService.sendNotifications(customerNotification,OWN_ACCOUNT_TRANSACTION,metadata,userDTO);
       }
       
        log.info("Total time taken for {} strategy {} milli seconds ", htmlEscape(request.getServiceType()), htmlEscape(Long.toString(between(start, now()).toMillis())));
        prepareAndCallPostTransactionActivity(metadata,fundTransferRequest,request,fundTransferResponse,conversionResult);
        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .transactionRefNo(validationResult.getTransactionRefNo())
                .build();
    }

    private void prepareAndCallPostTransactionActivity(RequestMetaData metadata, FundTransferRequest fundTransferRequest, FundTransferRequestDTO request, FundTransferResponse fundTransferResponse, CurrencyConversionDto conversionResult) {
        boolean isSuccess = MwResponseStatus.S.equals(fundTransferResponse.getResponseDto().getMwResponseStatus());
        if(goldSilverTransfer(request) && isSuccess){
            if(buyRequest(request)){
                fundTransferRequest.setNotificationType(NotificationType.GOLD_SILVER_BUY_SUCCESS);
                fundTransferRequest.setSrcAmount(conversionResult.getAccountCurrencyAmount());
                fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getDestinationCurrency()));
            }
            else{
                fundTransferRequest.setNotificationType(NotificationType.GOLD_SILVER_SELL_SUCCESS);
                fundTransferRequest.setAmount(conversionResult.getTransactionAmount());
                fundTransferRequest.setTransferType(getTransferType(fundTransferRequest.getSourceCurrency()));
            }
            postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest);
        }
        else if(isSuccess){
            fundTransferRequest.setTransferType(OWN_ACCOUNT);
            fundTransferRequest.setNotificationType(NotificationType.LOCAL);
            fundTransferRequest.setStatus(MwResponseStatus.S.getName());
            postTransactionService.performPostTransactionActivities(metadata, fundTransferRequest);
        }
    }

    private CustomerNotification populateCustomerNotification(String transactionRefNo, FundTransferRequestDTO requestDTO, BigDecimal amount, RequestMetaData metadata) {
        CustomerNotification customerNotification =new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(requestDTO.getTxnCurrency());
        customerNotification.setTxnRef(transactionRefNo);
        customerNotification.setSegment(digitalUserSegment.getCustomerCareInfo(metadata.getSegment()));
        return customerNotification;
    }

    private String getBeneficiaryCode(FundTransferRequestDTO request) {
        if(goldSilverTransfer(request)){
            return request.getCurrency();
        }
        else return request.getServiceType();
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, AccountDetailsDTO destinationAccount, BigDecimal exchangeRate, LimitValidatorResponse validationResult) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
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
                .exchangeRate(exchangeRate)
                .txnCurrency(request.getTxnCurrency())
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
                .build();

    }


    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
        return LOCAL_CURRENCY.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency(LOCAL_CURRENCY);

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private boolean isCurrencySame(AccountDetailsDTO destinationAccount, AccountDetailsDTO sourceAccount) {
        return destinationAccount.getCurrency().equalsIgnoreCase(sourceAccount.getCurrency());
    }

    //convert to sourceAccount from destAccount
    private CurrencyConversionDto getCurrencyExchangeObject(BigDecimal transactionAmount, AccountDetailsDTO destAccount, AccountDetailsDTO sourceAccount) {
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccount.getNumber());
        currencyRequest.setAccountCurrency(sourceAccount.getCurrency());
        currencyRequest.setTransactionCurrency(destAccount.getCurrency());
        currencyRequest.setTransactionAmount(transactionAmount);
        return maintenanceService.convertBetweenCurrencies(currencyRequest);

    }

    private boolean goldSilverTransfer(FundTransferRequestDTO request){
        return (ServiceType.XAU.getName().equals(request.getCurrency()) || ServiceType.XAG.getName().equals(request.getCurrency()));
    }

    private boolean buyRequest(FundTransferRequestDTO request){
        return request.getSrcAmount() == null;
    }

    //convert to sourceAccount from destAccount where amount is in src currency
    private CurrencyConversionDto getExchangeObjectForSrcAmount(BigDecimal transactionAmount, AccountDetailsDTO destAccount, AccountDetailsDTO sourceAccount) {
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccount.getNumber());
        currencyRequest.setAccountCurrency(sourceAccount.getCurrency());
        currencyRequest.setAccountCurrencyAmount(transactionAmount);
        currencyRequest.setTransactionCurrency(destAccount.getCurrency());
        return maintenanceService.convertBetweenCurrencies(currencyRequest);
    }

    private String getTransferType(String currency){
        if(ServiceType.XAU.getName().equals(currency)){
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
}