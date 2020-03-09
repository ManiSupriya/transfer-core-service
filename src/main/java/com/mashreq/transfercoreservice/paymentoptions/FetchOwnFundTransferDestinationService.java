package com.mashreq.transfercoreservice.paymentoptions;

import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Service
@RequiredArgsConstructor
public class FetchOwnFundTransferDestinationService implements FetchPaymentOptionsService {

    private final AccountService accountService;

    @Override
    public PaymentsOptionsResponse getPaymentOptions(PaymentOptionRequest request) {
        List<AccountDetailsDTO> accounts = accountService.getAccountsFromCore(request.getCifId())
                .stream()
                .filter(PaymentPredicates.fundTransferOwnAccountFilterDestination())
                .collect(Collectors.toList());

        return PaymentsOptionsResponse.builder()
                .accounts(accounts)
                .build();
    }
}
