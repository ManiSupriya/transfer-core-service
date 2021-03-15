package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NOT_FOUND;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CifProductsDto;
import com.mashreq.transfercoreservice.client.dto.CoreAccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
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

	public static final String TRUE = "true";
	private final AccountClient accountClient;
	private final AccountCardLessCashQueryService accountCardLessCashQueryService;
	private final AccountCardLessCashRequestService accountCardLessCashRequestService;
	private final AccountCardLessCashBlockRequestService accountCardLessCashBlockRequestService;
	private final FeesExternalConfig feeCodeConfig;
	private final AsyncUserEventPublisher asyncUserEventPublisher;

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

	public List<AccountDetailsDTO> getAccountsFromCoreWithDefaults(final String cifId) {
		log.info("Fetching accounts for cifId {} ", cifId);
		try {
			Response<CifProductsDto> cifProductsResponse = accountClient.searchAccounts(cifId, null);

			if (ResponseStatus.ERROR == cifProductsResponse.getStatus() || isNull(cifProductsResponse.getData())) {
				log.warn("Not able to fetch accounts, returning empty list instead");
				return Collections.emptyList();
			}

			List<AccountDetailsDTO> accounts = convertResponseToAccounts(cifProductsResponse.getData());
			log.info("{} Accounts fetched for cif id {} ", accounts.size(), cifId);
			return accounts;

		} catch (Exception e) {
			log.error("Error occurred while calling account client {} ", e);
			return Collections.emptyList();
		}
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
}
