package com.mashreq.transfercoreservice.client.service;

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenReq;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CifProductsDto;
import com.mashreq.transfercoreservice.client.dto.CoreAccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NOT_FOUND;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

	public List<AccountDetailsDTO> getAccountsFromCore(final String cifId) {
		log.info("Fetching accounts for cifId {} ", cifId);
		Response<CifProductsDto> cifProductsResponse = accountClient.searchAccounts(cifId, null);

		if (ResponseStatus.ERROR == cifProductsResponse.getStatus() || isNull(cifProductsResponse.getData())) {
			log.warn("Not able to fetch accounts, returning empty list instead");
			return Collections.emptyList();
		}

		List<AccountDetailsDTO> accounts = convertResponseToAccounts(cifProductsResponse.getData());
		log.info("{} Accounts fetched for cif id {} ", accounts.size(), cifId);
		return accounts;

	}

	public SearchAccountDto getAccountDetailsFromCore(final String accountNumber) {
		log.info("Fetching account details for accountNumber {} ", accountNumber);
		Response<CoreAccountDetailsDTO> response = accountClient.getAccountDetails(accountNumber);

		if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
			log.warn("Not able to fetch accounts");
			GenericExceptionHandler.handleError(ACC_EXTERNAL_SERVICE_ERROR,
					ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(response));
		}
		log.info("Accounts fetched for account number {}  ", accountNumber);

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
		return AccountDetailsDTO.builder().accountName(coreAccount.getAccountName())
				.availableBalance(convertStringToBigDecimal(coreAccount.getAvailableBalance()))
				.customerName(coreAccount.getCustomerName()).accountName(coreAccount.getAccountName())
				.schemeType(coreAccount.getAccountType().getSchemaType())
				.accountType(coreAccount.getAccountType().getAccountType()).currency(coreAccount.getCurrency())
				.number(coreAccount.getNumber()).status(coreAccount.getStatus()).segment("conventional")
				.branchCode(coreAccount.getBranch()).build();
	}

	private List<AccountDetailsDTO> convertResponseToAccounts(CifProductsDto cifProductsDto) {
		return cifProductsDto.getAccounts().stream().map(coreAccount -> convertCoreAccountsToAccountDTO(coreAccount))
				.sorted(Comparator.comparing(AccountDetailsDTO::getSchemeType)).collect(Collectors.toList());
	}

	private static BigDecimal convertStringToBigDecimal(String bigDecimalValue) {
		return isBlank(bigDecimalValue) ? BigDecimal.ZERO : new BigDecimal(bigDecimalValue);
	}

	public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(final String accountNumber,
			final BigInteger remitNumDays) {
		log.info("Fetching results for accountNumber {} ", accountNumber);

		Response<List<CardLessCashQueryResponse>> cardLessCashQueryResponse = null;
		try {
			cardLessCashQueryResponse = accountClient.cardLessCashRemitQuery(accountNumber, remitNumDays);

			if (isNotBlank(cardLessCashQueryResponse.getErrorCode())) {
				log.warn("Not able to fetch results, returning empty list instead");
				GenericExceptionHandler.handleError(null, cardLessCashQueryResponse.getErrorCode(), cardLessCashQueryResponse.getMessage());
			}

		} catch (Exception e) {
			log.error("Error occurred while calling query client {} ", e);
			GenericExceptionHandler.handleError(ACC_EXTERNAL_SERVICE_ERROR,
					ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), ACC_EXTERNAL_SERVICE_ERROR.getCustomErrorCode());
		}

		return cardLessCashQueryResponse;
	}

	public Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest) {
		log.info("blockRequest {} ", blockRequest);

		Response<CardLessCashBlockResponse> cardLessCashBlockResponse = null;
		try {
			cardLessCashBlockResponse = accountClient.blockCardLessCashRequest(blockRequest);

			if (isNotBlank(cardLessCashBlockResponse.getErrorCode())) {
				log.warn("Not able to block request for cashless card");
				GenericExceptionHandler.handleError(null, cardLessCashBlockResponse.getErrorCode(), cardLessCashBlockResponse.getMessage());
			}

		} catch (Exception e) {
			log.error("Error occurred while block request for cashless card {} ", e);
			GenericExceptionHandler.handleError(ACC_EXTERNAL_SERVICE_ERROR,
					ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(cardLessCashBlockResponse));

		}
		return cardLessCashBlockResponse;
	}

	public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(
			CardLessCashGenerationRequest cardLessCashGenerationRequest) {
		log.info("cardLessCashGenerationRequest {} ", cardLessCashGenerationRequest);
		
		CardLessCashGenReq cardLessCashGenReq = CardLessCashGenReq.builder()
				.accountNumber(cardLessCashGenerationRequest.getAccountNo())
				.amount(cardLessCashGenerationRequest.getAmount()).mobileNo(cardLessCashGenerationRequest.getMobileNo())
				.fees(cardLessCashGenerationRequest.getFees()).build();
		
		Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = null;
		try {
			cardLessCashGenerationResponse = accountClient
					.cardLessCashRemitGenerationRequest(cardLessCashGenReq);

			if (isNotBlank(cardLessCashGenerationResponse.getErrorCode())) {
				log.warn("Not able to generate request for cashless card");
				GenericExceptionHandler.handleError(null, cardLessCashGenerationResponse.getErrorCode(), cardLessCashGenerationResponse.getMessage());
			}

		} catch (Exception e) {
			log.error("Error occurred while generate request for cashless card {} ", e);
			GenericExceptionHandler.handleError(ACC_EXTERNAL_SERVICE_ERROR,
					ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), ACC_EXTERNAL_SERVICE_ERROR.getCustomErrorCode());

		}

		return cardLessCashGenerationResponse;
	}
}
