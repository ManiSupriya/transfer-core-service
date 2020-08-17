package com.mashreq.transfercoreservice.fundtransfer.strategy;

import static java.lang.Long.valueOf;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.esbcore.validator.EsbResponseValidator;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.AccountDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.AccountDetailsEaiServices;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferEaiServices;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.FinTxnNoValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.PaymentPurposeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.middleware.EsbRequestsCreator;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class InternationalFundTransferStrategy implements FundTransferStrategy {

    private static final String INTERNATIONAL_PRODUCT_ID = "DBFC";
    private static final String INDIVIDUAL_ACCOUNT = "I";
    private static final String ROUTING_CODE_PREFIX = "//";
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountService accountService;
	private final SoapServiceProperties appProperties;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final MaintenanceService maintenanceService;
    private final MobCommonService mobCommonService;
    private final DealValidator dealValidator;
    private final EsbRequestsCreator esbRequestsCreator;
    private final WebServiceClient webServiceClient;
	private final ConversionService conversionService;

    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;
    private final AsyncUserEventPublisher auditEventPublisher;

    private final HashMap<String, String> countryToCurrencyMap = new HashMap<>();

//    @Value("${app.local.transaction.code:015}")
//    private String transactionCode;

    //Todo: Replace with native currency fetched from API call
    @PostConstruct
    private void initCountryToNativeCurrencyMap() {
        countryToCurrencyMap.put("IN", "INR");
        countryToCurrencyMap.put("AU", "AUD");
        countryToCurrencyMap.put("CA", "CAD");
        countryToCurrencyMap.put("NZ", "NZD");
        countryToCurrencyMap.put("UK", "GBP");
        countryToCurrencyMap.put("US", "USD");
    }

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
		Optional<SearchAccountDto> fromAccount = getSearchAccountDto(request.getFromAccount(), metadata);
		if (!fromAccount.isPresent()) {
			auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.RESOURCE_NOT_MACTH, metadata, CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
         			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.toString(), TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage(),
       			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
			 GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH, TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
        }
		Optional<SearchAccountDto> toAccount = getSearchAccountDto(request.getToAccount(), metadata);
		if (!toAccount.isPresent()) {
			auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.RESOURCE_NOT_MACTH, metadata, CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
         			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.toString(), TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage(),
       			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
			 GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH, TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
        }
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(),
                "", INDIVIDUAL_ACCOUNT);
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));


        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()));
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));

        //Deal Validator
        responseHandler(dealValidator.validate(request, metadata, validationContext));
        
        //Balance Validation
        final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("to-account-currency", beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);

        final BigDecimal transferAmountInSrcCurrency = isCurrencySame(beneficiaryDto,sourceAccountDetailsDTO)
                ? request.getAmount()
                : getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validationContext));


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,transferAmountInSrcCurrency);
        final LimitValidatorResponse validationResult = limitValidator.validateWithProc(userDTO, request.getServiceType(), limitUsageAmount, metadata, null);


        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, sourceAccountDetailsDTO, beneficiaryDto);
   	 String msgId = getUniqueIdForRequest(fundTransferRequest);
        log.info("International Fund transfer initiated.......");
        CurrencyConversionDto currencyConversionDto = convertAmountInLocalCurrency(request.getDealNumber(), sourceAccountDetailsDTO, transferAmountInSrcCurrency);
        validateAccountForTransfer(fromAccount.get(), currencyConversionDto, metadata);
        
        FundTransferEaiServices req = esbRequestsCreator.createFundTransferRequest(request.getFromAccount(),
				request.getAmount(), request.getCurrency(), request.getToAccount(), beneficiaryDto.getBankBranchName(),
				request.getCurrency(), request.getDealNumber(), currencyConversionDto);
		FundTransferEaiServices resp = new FundTransferEaiServices(webServiceClient.exchange(req));
		 final FundTransferResType.Transfer transfer = resp.getBody().getFundTransferRes().getTransfer().get(0);
	        final ErrorType exceptionDetails = resp.getBody().getExceptionDetails();
	        if (isSuccessfull(resp)) {
	            auditEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.FUND_TRANSFER_MW_CALL, metadata, getRemarks(fundTransferRequest), msgId);
	            log.info("Fund transferred successfully to account [ {} ]", request.getToAccount());
	            final CoreFundTransferResponseDto coreFundTransferResponseDto = constructFTResponseDTO(transfer, exceptionDetails, MwResponseStatus.S);
	            return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
	        }
        final CoreFundTransferResponseDto coreFundTransferResponseDto = constructFTResponseDTO(transfer, exceptionDetails, MwResponseStatus.S);
        return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
    }

    private boolean isCurrencySame(BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        return sourceAccountDetailsDTO.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency());
    }

    private CurrencyConversionDto convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency("AED");

        return maintenanceService.convertCurrency(currencyConversionRequestDto);
    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
    	CurrencyConversionDto currencyConversionDto = convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    	BigDecimal transactionAmt=  currencyConversionDto.getTransactionAmount();
        return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : transactionAmt;
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto,
                                              AccountDetailsDTO sourceAccountDetailsDTO) {
        BigDecimal amtToBePaidInSrcCurrency;
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency(beneficiaryDto.getBeneficiaryCurrency());
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto) {
        final FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .productId(INTERNATIONAL_PRODUCT_ID)
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(beneficiaryDto.getAccountNumber())
                .purposeCode(request.getPurposeCode())
                .purposeDesc(request.getPurposeDesc())
                .chargeBearer(request.getChargeBearer())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(accountDetails.getCurrency())
                .sourceBranchCode(accountDetails.getBranchCode())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .destinationCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .beneficiaryAddressOne(beneficiaryDto.getAddressLine1())
                .beneficiaryAddressTwo(beneficiaryDto.getAddressLine2())
                .beneficiaryAddressThree(beneficiaryDto.getAddressLine3())
                .transactionCode("15")
                .build();

        return enrichFundTransferRequestByCountryCode(fundTransferRequest, beneficiaryDto);
    }

    private FundTransferRequest enrichFundTransferRequestByCountryCode(FundTransferRequest request, BeneficiaryDto beneficiaryDto) {
        List<CountryMasterDto> countryList = maintenanceService.getAllCountries("MOB", "AE", Boolean.TRUE);
        final Optional<CountryMasterDto> countryDto = countryList.stream()
                .filter(country -> country.getCode().equals(beneficiaryDto.getBeneficiaryCountryISO()))
                .findAny();
        if (countryDto.isPresent()) {
            final CountryMasterDto countryMasterDto = countryDto.get();
            if (StringUtils.isNotBlank(countryMasterDto.getRoutingCode()) && request.getDestinationCurrency()
                    .equals(countryToCurrencyMap.get(beneficiaryDto.getBeneficiaryCountryISO()))) {

                return request.toBuilder()
                        .awInstBICCode(ROUTING_CODE_PREFIX + beneficiaryDto.getRoutingCode())
                        .awInstName(beneficiaryDto.getSwiftCode())
                        .build();
            }
        }
        return request.toBuilder()
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .awInstName(beneficiaryDto.getBankName())
                .build();
    }
    
    private String getRemarks(FundTransferRequest request) {
        return String.format("From Account = %s, To Account = %s, Amount = %s, SrcAmount= %s, Destination Currency = %s, Source Currency = %s," +
                        " Financial Transaction Number = %s, Beneficiary full name = %s, Swift code= %s, Beneficiary bank branch = %s ",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getSrcAmount(),
                request.getDestinationCurrency(),
                request.getSourceCurrency(),
                request.getFinTxnNo(),
                request.getBeneficiaryFullName(),
                request.getAwInstBICCode(),
                request.getAwInstName());
    }

    private CoreFundTransferResponseDto constructFTResponseDTO(FundTransferResType.Transfer transfer, ErrorType exceptionDetails, MwResponseStatus s) {
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setHostRefNo(transfer.getTransactionRefNo());
        coreFundTransferResponseDto.setExternalErrorMessage(exceptionDetails.getData());
        coreFundTransferResponseDto.setMwReferenceNo(transfer.getTransactionRefNo());
        coreFundTransferResponseDto.setMwResponseDescription(exceptionDetails.getErrorDescription());
        coreFundTransferResponseDto.setMwResponseStatus(s);
        coreFundTransferResponseDto.setMwResponseCode(exceptionDetails.getErrorCode());
                return coreFundTransferResponseDto;
    }

    private boolean isSuccessfull(EAIServices response) {
        log.info("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.error("Exception during fund transfer. Code: {} , Description: {}", response.getBody()
                    .getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getData());

            return false;
        }
        return true;
    }

    private String getUniqueIdForRequest(FundTransferRequest request) {
        if(!StringUtils.isEmpty(request.getLimitTransactionRefNo())){
            log.info("returning refNo");
            return request.getLimitTransactionRefNo();
        }
        else if(request.getChannelTraceId().length() >16){
            return DateTimeFormatter.ofPattern("yyMMddHHmmssSSS").format(LocalDateTime.now());
        }
        else return request.getChannelTraceId();
    }
    
    private Optional<SearchAccountDto> getSearchAccountDto(String accountNumber, RequestMetaData metadata) {
		AccountDetailsDto accountDetails = getAccountDetails(accountNumber, metadata);
		if (accountDetails == null || CollectionUtils.isEmpty(accountDetails.getConnectedAccounts())) {
			return Optional.empty();
		}
		return accountDetails.getConnectedAccounts().stream().filter(a -> a.getNumber().equals(accountNumber))
				.findAny();
	}
    
    /**
	 * Returns account details
	 */
	public AccountDetailsDto getAccountDetails(String accNo, RequestMetaData metadata) {
		AccountDetailsEaiServices request = esbRequestsCreator.createEsbAccountDetaisRequest(accNo);
		AccountDetailsEaiServices resp = new AccountDetailsEaiServices(webServiceClient.exchange(request));
		EsbResponseValidator.validate(
				appProperties.getServiceCodes().getSearchAccountDetails(),
				CommonConstants.SEARCH_ACCOUNT_DETAILS, resp, null);
		return auditEventPublisher.publishEventLifecycle(
                () -> conversionService.convert(resp.getBody().getAccountDetailsRes(),
				AccountDetailsDto.class),FundTransferEventType.DEAL_VALIDATION, metadata, CommonConstants.FUND_TRANSFER);
	}
	
	private void validateAccountForTransfer(SearchAccountDto account, CurrencyConversionDto currencyConversionDto, RequestMetaData metadata) {
		if (currencyConversionDto.getAccountCurrencyAmount()
				.compareTo(new BigDecimal(account.getAvailableBalance())) > 0) {
			log.error("There is not enough resources in account " + account.getNumber() + " to transfer "
					+ currencyConversionDto.getAccountCurrencyAmount());
			auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.RESOURCE_NOT_MACTH, metadata, CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
         			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.toString(), TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage(),
       			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
			 GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH, TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
		}
	}


}
