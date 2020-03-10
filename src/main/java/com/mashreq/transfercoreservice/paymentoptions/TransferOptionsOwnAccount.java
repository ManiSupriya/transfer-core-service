package com.mashreq.transfercoreservice.paymentoptions;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferOptionsOwnAccount implements FetchPaymentOptionsService {

    private final AccountService accountService;

    @Override
    public PaymentsOptionsResponse getPaymentOptions(PaymentOptionRequest request) {

        List<AccountDetailsDTO> coreAccounts = accountService.getAccountsFromCore(request.getCifId());

        List<AccountDetailsDTO> sourceAccounts = coreAccounts.stream()
                .filter(PaymentPredicates.fundTransferAccountFilterSource())
                .sorted(Comparator.comparing(AccountDetailsDTO::getAvailableBalance).reversed())
                .collect(Collectors.toList());
        log.info("Found {} Source Accounts ", sourceAccounts);
        log.debug("Source Accounts {} ", sourceAccounts);

        Optional<AccountDetailsDTO> defaultSourceAccountOptional = sourceAccounts.stream().findFirst();

        log.info("Default Source Accounts present = {} ", defaultSourceAccountOptional.isPresent());

        PaymentOptionPayLoad source = PaymentOptionPayLoad.builder()
                .accounts(sourceAccounts)
                .defaultAccount(defaultSourceAccountOptional.orElse(null))
                .build();

        List<AccountDetailsDTO> destinationAccounts = coreAccounts.stream()
                .filter(PaymentPredicates.fundTransferOwnAccountFilterDestination())
                .sorted(Comparator.comparing(AccountDetailsDTO::getAvailableBalance))
                .collect(Collectors.toList());

        log.info("Found {} Destination Accounts ", destinationAccounts);
        log.debug("Destination Accounts {} ", destinationAccounts);

        Optional<AccountDetailsDTO> defaultDestinationAccountOptional = destinationAccounts.stream()
                .filter(account -> !account.getNumber().equals(defaultSourceAccountOptional.orElse(null)))
                .findFirst();

        log.info("Default Destination Accounts present = {} ", defaultDestinationAccountOptional.isPresent());

        PaymentOptionPayLoad destination = PaymentOptionPayLoad.builder()
                .accounts(destinationAccounts)
                .defaultAccount(defaultDestinationAccountOptional.orElse(null))
                .build();

        return PaymentsOptionsResponse.builder()
                .source(source)
                .destination(destination)
                .build();
    }
}
