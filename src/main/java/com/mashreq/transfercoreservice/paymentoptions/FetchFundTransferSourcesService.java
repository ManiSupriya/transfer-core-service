package com.mashreq.transfercoreservice.paymentoptions;

import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Service
@RequiredArgsConstructor
public class FetchFundTransferSourcesService implements FetchPaymentOptionsService {

    private final AccountService accountService;

    @Override
    public PaymentsOptionsResponse getPaymentOptions(PaymentOptionRequest request) {
        List<AccountDetailsDTO> accounts = accountService.getAccountsFromCore(request.getCifId())
                .stream()
                .filter(PaymentPredicates.fundTransferAccountFilterSource())
                .collect(Collectors.toList());

        //TODO : This should based on usage pull this up by payments history
        Optional<AccountDetailsDTO> defaultAccountOptional = accounts.stream()
                .filter(account -> account.getAvailableBalance().compareTo(getMinAmountToBeAvailable(request)) == 1)
                .findFirst();

        return PaymentsOptionsResponse.builder()
                .accounts(accounts)
                .defaultAccount(defaultAccountOptional.orElse(null))
                .build();
    }
}
