package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.mobcommons.utils.ContextCacheKeysSuffix.ACCOUNTS;
import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;

import com.mashreq.mobcommons.cache.MobRedisService;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.*;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenReq;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.common.FeesExternalConfig;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

	private static final TypeReference<Map<String, Object>> HASH_MAP_MAP_TYPE_REF = new TypeReference<Map<String, Object>>() {
	};
	public static final String TRUE = "true";
	public static final String ACCOUNT_NUMBERS = "account-numbers";
	private final MobRedisService redisService;
	private final AccountClient accountClient;
	private final AccountCardLessCashQueryService accountCardLessCashQueryService;
	private final AccountCardLessCashRequestService accountCardLessCashRequestService;
	private final AccountCardLessCashBlockRequestService accountCardLessCashBlockRequestService;
	private final FeesExternalConfig feeCodeConfig;
	private final AsyncUserEventPublisher asyncUserEventPublisher;
	private final UserSessionCacheService userSessionCacheService;
	private final MobRedisService mobRedisService;

	public static final Predicate<Response<IbanDetailsDto>> isResponseNotValid = (response) ->
			isNull(response) || isNull(response.getData()) || ResponseStatus.ERROR == response.getStatus() ;

	public List<AccountDetailsDTO> getAccountsFromCore(final String cifId) {
		log.info("Fetching accounts for cifId {} ", htmlEscape(cifId));
		Response<CifProductsDto> cifProductsResponse = accountClient.searchAccounts(cifId, null);

		if (ResponseStatus.ERROR == cifProductsResponse.getStatus() || isNull(cifProductsResponse.getData())) {
			log.warn("Not able to fetch accounts, returning empty list instead");
			return Collections.emptyList();
		}

		List<AccountDetailsDTO> accounts = convertResponseToAccounts(cifProductsResponse.getData());
		log.info("{} Accounts fetched for cif id {} ", htmlEscape(Integer.toString(accounts.size())), htmlEscape(cifId));
		return accounts;

	}
   public void getAccountsIfNotInCache(final RequestMetaData metaData) {
	   final Map<String, Object> accountContext = redisService.get(metaData.getUserCacheKey()  + ACCOUNTS.getSuffix(), HASH_MAP_MAP_TYPE_REF);
	   if (MapUtils.isEmpty(accountContext)) {
		   log.info("Account Context is empty, getting from core");
		   setAccountsFromCoreToCache(metaData);
	   }
   }

	private void setAccountsFromCoreToCache(final RequestMetaData metaData){
		final List<AccountDetailsDTO> accountsFromCore = getAccountsFromCore(metaData.getPrimaryCif());
		List<String> accountNumberList = Stream.of(accountsFromCore).flatMap(list -> list.stream())
				.map(AccountDetailsDTO::getNumber)
				.collect(Collectors.toList());
		final Map<String, Object> accountsContext = new HashMap<>();
		accountsContext.put(ACCOUNT_NUMBERS, accountNumberList);
		final String accountsContextCacheKey = metaData.getUserCacheKey() + ACCOUNTS.getSuffix();
		log.info("Setting account context to cache");
		redisService.setWithDefaultTTL(accountsContextCacheKey, accountsContext);
	}

	public SearchAccountDto getAccountDetailsFromCore(final String accountNumber) {
		log.info("Fetching account details for accountNumber {} ", htmlEscape(accountNumber));
		Response<CoreAccountDetailsDTO> response = accountClient.getAccountDetails(accountNumber);

		if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
			log.warn("Not able to fetch accounts");
			GenericExceptionHandler.handleError(ACC_EXTERNAL_SERVICE_ERROR,
					ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(response));
		}
		log.info("Accounts fetched for account number {}  ", htmlEscape(accountNumber));

		Optional<SearchAccountDto> searchAccountOpt = response.getData().getConnectedAccounts().stream().findFirst();

		if (!searchAccountOpt.isPresent()) {
			log.warn("Not able to fetch accounts");
			GenericExceptionHandler.handleError(ACCOUNT_NOT_FOUND, ACCOUNT_NOT_FOUND.getErrorMessage(),
					getErrorDetails(response));

		}
		return searchAccountOpt.get();

	}

	public boolean isAccountBelongsToMashreq(final String accountNo) {
		return Objects.nonNull(getAccountDetailsFromCore(accountNo));
	}

	private AccountDetailsDTO getConvertedAccountDetailsFromCore(final String accountNumber) {
		return convertCoreAccountsToAccountDTO(getAccountDetailsFromCore(accountNumber));
	}

	private AccountDetailsDTO convertCoreAccountsToAccountDTO(SearchAccountDto coreAccount) {
		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setAccountName(coreAccount.getAccountName());
		accountDetailsDTO.setAvailableBalance(convertStringToBigDecimal(coreAccount.getAvailableBalance()));
		accountDetailsDTO.setCustomerName(coreAccount.getCustomerName());
		accountDetailsDTO.setAccountName(coreAccount.getAccountName());
		accountDetailsDTO.setSchemeType(coreAccount.getAccountType().getSchemaType());
		accountDetailsDTO.setAccountType(coreAccount.getAccountType().getAccountType());
		accountDetailsDTO.setCurrency(coreAccount.getCurrency());
		accountDetailsDTO.setNumber(coreAccount.getNumber());
		accountDetailsDTO.setStatus(coreAccount.getStatus());
		accountDetailsDTO.setSegment("conventional");
		accountDetailsDTO.setBranchCode(coreAccount.getBranch());
		return accountDetailsDTO;
	}

	private List<AccountDetailsDTO> convertResponseToAccounts(CifProductsDto cifProductsDto) {
		return cifProductsDto.getAccounts().stream().map(coreAccount -> convertCoreAccountsToAccountDTO(coreAccount))
				.sorted(Comparator.comparing(AccountDetailsDTO::getSchemeType)).collect(Collectors.toList());
	}

	private static BigDecimal convertStringToBigDecimal(String bigDecimalValue) {
		return isBlank(bigDecimalValue) ? BigDecimal.ZERO : new BigDecimal(bigDecimalValue);
	}

	public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(CardLessCashQueryRequest cardLessCashQueryRequest, RequestMetaData metaData) {
		log.info("cardless cash query request {} ", cardLessCashQueryRequest);
		return asyncUserEventPublisher.publishEventLifecycle(
                () -> Response.<List<CardLessCashQueryResponse>>builder().status(ResponseStatus.SUCCESS).data(accountCardLessCashQueryService.getResponse(cardLessCashQueryRequest).getData()).build(),
                FundTransferEventType.CARD_LESS_CASH_QUERY_DETAILS,
                metaData, CommonConstants.CARD_LESS_CASH
                );
	}

	public Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest, RequestMetaData metaData) {
		log.info("cardless cash blockRequest {} ", blockRequest);

		return asyncUserEventPublisher.publishEventLifecycle(
                () -> accountCardLessCashBlockRequestService.getResponse(blockRequest),
                FundTransferEventType.CARD_LESS_CASH_BLOCK_REQUEST,
                metaData, CommonConstants.CARD_LESS_CASH
                );

	}

	public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(
			CardLessCashGenerationRequest cardLessCashGenerationRequest, String userMobileNumber, RequestMetaData metaData) {
		log.info("cardLess cash request Generation {} ", cardLessCashGenerationRequest);

		CardLessCashGenReq cardLessCashGenReq = CardLessCashGenReq.builder()
				.accountNumber(cardLessCashGenerationRequest.getAccountNo())
				.amount(cardLessCashGenerationRequest.getAmount()).mobileNo(userMobileNumber)
				.fees(new BigDecimal(feeCodeConfig.getCardLessCashExternalFee())).build();
		return asyncUserEventPublisher.publishEventLifecycle(
                () -> accountCardLessCashRequestService.getResponse(cardLessCashGenReq),
                FundTransferEventType.CARD_LESS_CASH_GENERATION_REQUEST,
                metaData, CommonConstants.CARD_LESS_CASH
                );

	}

	public AccountDetailsDTO getAccountDetailsFromCache(final String accountNumber, RequestMetaData requestMetaData) {
		if(!userSessionCacheService.isMTAccountNumberBelongsToCif(accountNumber, requestMetaData.getUserCacheKey())){
			GenericExceptionHandler.handleError(ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF,ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF.getErrorMessage());
		}
		AccountDetailsDTO accountDetailsDTO = mobRedisService.get(userSessionCacheService.getAccountsDetailsCacheKey(requestMetaData, accountNumber), AccountDetailsDTO.class);
		if(accountDetailsDTO == null){
			log.info("[AccountService] cache miss for account details");
			accountDetailsDTO = getConvertedAccountDetailsFromCore(accountNumber);
		}
		return accountDetailsDTO;
	}
	public IbanDetailsDto getAccountDetailsByAccountNumber(final String accountNumber) {
		log.info("Fetching account details for accountNumber {} ", accountNumber);
		Response<IbanDetailsDto> response = null;
		try {
			response = accountClient.searchIban(accountNumber);
		} catch (Exception e) {
			log.error("Get Account Details by Account Number call failed for {}",accountNumber);
		}
		if (isResponseNotValid.test(response)) {
			log.error("Not able to get account details for {}",accountNumber);
		}
		// will remove response from logs once it's tested in UAT
		log.info("Get Account Details Call completed for accountNumber {} and the response is {}",accountNumber, response );
		return Optional.ofNullable(response).isPresent() ? response.getData() : null;
	}
}
