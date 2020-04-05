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

import java.util.Collections;
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

        //Fetch Accounts from Core
        List<AccountDetailsDTO> coreAccounts = accountService.getAccountsFromCoreWithDefaults(request.getCifId());

        //Extract Source Accounts
        List<AccountDetailsDTO> sourceAccounts = coreAccounts.stream()
                .filter(PaymentPredicates.fundTransferAccountFilterSource())
                .sorted(Comparator.comparing(AccountDetailsDTO::getAvailableBalance).reversed())
                .collect(Collectors.toList());
        log.info("Found {} Source Accounts ", sourceAccounts.size());
        log.debug("Source Accounts {} ", sourceAccounts.stream().map(AccountDetailsDTO::getNumber).collect(Collectors.joining(",")));


        // Compute Suggested Account
        Optional<AccountDetailsDTO> defaultSourceAccountOptional = sourceAccounts.stream().findFirst();
        log.info("Default Source Accounts present = {} ", defaultSourceAccountOptional.isPresent());

        // Construct Source Payload
        PaymentOptionPayLoad source = PaymentOptionPayLoad.builder()
                .accounts(sourceAccounts)
                .defaultAccount(defaultSourceAccountOptional.orElse(null))
                .build();

        // If only one Account present the return with only source payload
        if (sourceAccounts.size() == 1) {
            return returnWithSourcePayloadOnly(request, sourceAccounts, source);
        }

        List<AccountDetailsDTO> destinationAccounts = coreAccounts.stream()
                .filter(PaymentPredicates.fundTransferOwnAccountFilterDestination())
                .sorted(Comparator.comparing(AccountDetailsDTO::getAvailableBalance))
                .collect(Collectors.toList());

        log.info("Found {} Destination Accounts ", destinationAccounts.size());
        log.debug("Destination Accounts {} ", destinationAccounts.stream().map(AccountDetailsDTO::getNumber).collect(Collectors.joining(",")));

        PaymentOptionPayLoad destination = PaymentOptionPayLoad.builder()
                .accounts(destinationAccounts)
                .build();

        return PaymentsOptionsResponse.builder()
                .source(source)
                .destination(destination)
                .finTxnNo(isPayloadEmpty(sourceAccounts, destinationAccounts)
                        ? null
                        : FinTxnNumberGenerator.generate(request.getChannelName(), request.getCifId(), request.getPaymentOptionType()))
                .build();


    }

    private PaymentsOptionsResponse returnWithSourcePayloadOnly(PaymentOptionRequest request, List<AccountDetailsDTO> sourceAccounts, PaymentOptionPayLoad source) {
        log.info("Only one account present returning it as source payload = {} ", source);
        return PaymentsOptionsResponse.builder()
                .source(source)
                .finTxnNo(isPayloadEmpty(sourceAccounts, null)
                        ? null
                        : FinTxnNumberGenerator.generate(request.getChannelName(), request.getCifId(), request.getPaymentOptionType()))
                .destination(PaymentOptionPayLoad.builder().accounts(Collections.emptyList()).build())
                .build();
    }


}
