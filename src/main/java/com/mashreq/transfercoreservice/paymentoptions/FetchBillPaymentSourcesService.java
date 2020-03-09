package com.mashreq.transfercoreservice.paymentoptions;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.CardService;
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
public class FetchBillPaymentSourcesService implements FetchPaymentOptionsService {

    private final AccountService accountService;
    private final CardService cardService;

    @Override
    public PaymentsOptionsResponse getPaymentOptions(PaymentOptionRequest request) {
        List<CardDetailsDTO> cards = cardService.getCardsFromCore(request.getCifId(), CardType.CC)
                .stream()
                .filter(PaymentPredicates.billPaymentCardFilterSource())
                .collect(Collectors.toList());

        List<AccountDetailsDTO> accounts = accountService.getAccountsFromCore(request.getCifId())
                .stream()
                .filter(PaymentPredicates.billPaymentAccountFilterSource())
                .collect(Collectors.toList());

        Optional<CardDetailsDTO> defaultCardOptional = cards.stream()
                .filter(x -> x.getAvailableCreditLimit().compareTo(getMinAmountToBeAvailable(request)) == 1)
                .findFirst();

        Optional<AccountDetailsDTO> defaultAccountOptional = accounts.stream()
                .filter(account -> account.getAvailableBalance().compareTo(getMinAmountToBeAvailable(request)) == 1)
                .findFirst();

        if (defaultCardOptional.isPresent()) {

            return PaymentsOptionsResponse.builder()
                    .creditCards(cards)
                    .accounts(accounts)
                    .defaultCard(defaultCardOptional.get())
                    .build();

        } else if (defaultAccountOptional.isPresent()) {

            return PaymentsOptionsResponse.builder()
                    .creditCards(cards)
                    .accounts(accounts)
                    .defaultAccount(defaultAccountOptional.get())
                    .build();
        } else {

            return PaymentsOptionsResponse.builder()
                    .creditCards(cards)
                    .accounts(accounts)
                    .build();
        }
    }
}
