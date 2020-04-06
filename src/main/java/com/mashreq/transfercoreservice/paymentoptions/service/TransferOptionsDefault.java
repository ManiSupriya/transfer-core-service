package com.mashreq.transfercoreservice.paymentoptions.service;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentOptionPayLoad;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentOptionRequest;
import com.mashreq.transfercoreservice.paymentoptions.dto.PaymentsOptionsResponse;
import com.mashreq.transfercoreservice.paymentoptions.utils.PaymentPredicates;
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

        log.info("Found {} Source Accounts for {} ", request.getPaymentOptionType());
        List<AccountDetailsDTO> coreAccounts = accountService.getAccountsFromCoreWithDefaults(request.getCifId());

        List<AccountDetailsDTO> sourceAccounts = coreAccounts.stream()
                .filter(PaymentPredicates.fundTransferAccountFilterSource())
                .sorted(Comparator.comparing(AccountDetailsDTO::getAvailableBalance).reversed())
                .collect(Collectors.toList());
        log.info("Found {} Source Accounts ", sourceAccounts.size());
        log.debug("Source Accounts {} ", sourceAccounts.stream().map(AccountDetailsDTO::getNumber).collect(Collectors.joining(",")));

        Optional<AccountDetailsDTO> defaultSourceAccountOptional = sourceAccounts.stream().findFirst();

        log.info("Default Source Accounts present = {} ", defaultSourceAccountOptional.isPresent());

        final PaymentOptionPayLoad source = PaymentOptionPayLoad.builder()
                .accounts(sourceAccounts)
                .defaultAccount(defaultSourceAccountOptional.orElse(null))
                .build();

        return PaymentsOptionsResponse.builder()
                .source(source)
                .finTxnNo(isPayloadEmpty(sourceAccounts, null)
                        ? null
                        : FinTxnNumberGenerator.generate(request.getChannelName(), request.getCifId(), request.getPaymentOptionType()))
                .build();
    }
}
