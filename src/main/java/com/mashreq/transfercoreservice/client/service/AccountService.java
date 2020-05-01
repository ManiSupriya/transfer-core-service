package com.mashreq.transfercoreservice.client.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.client.ErrorUtils;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CifProductsDto;
import com.mashreq.transfercoreservice.client.dto.CoreAccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NOT_FOUND;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_ACC_NOT_MATCH;
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

        if (isNotBlank(cifProductsResponse.getErrorCode())) {
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

        if (isNotBlank(response.getErrorCode()) || TRUE.equalsIgnoreCase(response.getHasError())) {
            log.warn("Not able to fetch accounts");
            GenericExceptionHandler.handleError(ACCOUNT_NOT_FOUND, ACCOUNT_NOT_FOUND.getErrorMessage(), getErrorDetails(response));
        }
        log.info("{} Accounts fetched for account number {}  ", accountNumber);

        Optional<SearchAccountDto> searchAccountOpt = response.getData().getConnectedAccounts().stream().findFirst();

        if (!searchAccountOpt.isPresent()) {
            log.warn("Not able to fetch accounts");
            GenericExceptionHandler.handleError(ACCOUNT_NOT_FOUND, ACCOUNT_NOT_FOUND.getErrorMessage(), getErrorDetails(response));

        }
        return searchAccountOpt.get();

    }

    public List<AccountDetailsDTO> getAccountsFromCoreWithDefaults(final String cifId) {
        log.info("Fetching accounts for cifId {} ", cifId);
        try {
            Response<CifProductsDto> cifProductsResponse = accountClient.searchAccounts(cifId, null);

            if (isNotBlank(cifProductsResponse.getErrorCode())) {
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
        return AccountDetailsDTO.builder()
                .accountName(coreAccount.getAccountName())
                .availableBalance(convertStringToBigDecimal(coreAccount.getAvailableBalance()))
                .customerName(coreAccount.getCustomerName())
                .accountName(coreAccount.getAccountName())
                .schemeType(coreAccount.getAccountType().getSchemaType())
                .accountType(coreAccount.getAccountType().getAccountType())
                .currency(coreAccount.getCurrency())
                .number(coreAccount.getNumber())
                .status(coreAccount.getStatus())
                .segment("conventional")
                .branchCode(coreAccount.getBranch())
                .build();
    }

    private List<AccountDetailsDTO> convertResponseToAccounts(CifProductsDto cifProductsDto) {
        return cifProductsDto.getAccounts().stream()
                .map(coreAccount -> convertCoreAccountsToAccountDTO(coreAccount))
                .sorted(Comparator.comparing(AccountDetailsDTO::getSchemeType))
                .collect(Collectors.toList());
    }

    private static BigDecimal convertStringToBigDecimal(String bigDecimalValue) {
        return isBlank(bigDecimalValue) ? BigDecimal.ZERO : new BigDecimal(bigDecimalValue);
    }
}
