package com.mashreq.transfercoreservice.paymentoptions.service;

import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.paymentoptions.utils.PaymentPredicates;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentOptionPayLoad;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentOptionRequest;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentsOptionsResponse;
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
public class TransferOptionsDefault implements FetchPaymentOptionsService {

    private final AccountService accountService;

    @Override
    public PaymentsOptionsResponse getPaymentOptions(PaymentOptionRequest request) {

        List<AccountDetailsDTO> coreAccounts = accountService.getAccountsFromCore(request.getCifId());

        List<AccountDetailsDTO> sourceAccounts = coreAccounts.stream()
                .filter(PaymentPredicates.fundTransferAccountFilterSource())
                .sorted(Comparator.comparing(AccountDetailsDTO::getAvailableBalance).reversed())
                .collect(Collectors.toList());
        log.info("Found {} Source Accounts ", sourceAccounts.size());
        log.debug("Source Accounts {} ", sourceAccounts.stream().map(AccountDetailsDTO::getNumber).collect(Collectors.joining(",")));

        Optional<AccountDetailsDTO> defaultSourceAccountOptional = sourceAccounts.stream().findFirst();

        log.info("Default Source Accounts present = {} ", defaultSourceAccountOptional.isPresent());

        PaymentOptionPayLoad source = PaymentOptionPayLoad.builder()
                .accounts(sourceAccounts)
                .defaultAccount(defaultSourceAccountOptional.orElse(null))
                .build();

        return PaymentsOptionsResponse.builder()
                .source(source)
                .finTxnNo(FinTxnNumberGenerator.generate(request.getPaymentOptionType()))
                .build();
    }
}
