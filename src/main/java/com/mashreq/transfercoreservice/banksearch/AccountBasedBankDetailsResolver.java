package com.mashreq.transfercoreservice.banksearch;


import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.dto.BankResolverRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.mashreq.transfercoreservice.errors.ExceptionUtils.genericException;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_NOT_FOUND_FOR_BANK_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SWIFT_CODE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountBasedBankDetailsResolver implements BankDetailsResolver {
    private final BankRepository bankRepository;
    private final AccountService accountService;

    @Value("${app.local.bank.code}")
    private String localBankCode;


    @Override
    public List<BankResultsDto> getBankDetails(BankResolverRequestDto bankResolverRequestDto) {

        return getBankResultForIban(bankResolverRequestDto.getJourneyType(), bankResolverRequestDto.getIdentifier(),
                bankResolverRequestDto.getBankCode());

    }

    private List<BankResultsDto> getBankResultForIban(String journeyType, String accountNumber, String bankCode) {
        if ("MT".equals(journeyType)) {
            List<BankDetails> bankDetailsList = bankRepository.findByBankCode(bankCode).orElseThrow(() -> genericException(BANK_NOT_FOUND_FOR_BANK_CODE));
            BankDetails bankDetail = bankDetailsList.stream().filter(bank -> bankCode.equals(bank.getBankCode())).findAny()
                    .orElseThrow(() -> genericException(BANK_NOT_FOUND_FOR_BANK_CODE));

            //Todo: Remove this If clause once you update all the SWIFT code in bank_ms
            if (StringUtils.isBlank(bankDetail.getSwiftCode())) {
                throw genericException(INVALID_SWIFT_CODE);
            }

            BankResultsDto bankResults = new BankResultsDto();
            bankResults.setSwiftCode(bankDetail.getSwiftCode());
            bankResults.setIdentifierType(BankCodeType.ACCOUNT.getName());
            bankResults.setBankName(bankDetail.getBankName());
            bankResults.setBankCode(bankCode);

            if (localBankCode.equals(bankDetail.getBankCode()) && accountService.isAccountBelongsToMashreq(accountNumber)) {
                log.info("Result of account belongs to mashreq check for AcctNum: {} , isMashreqAcct: {}",
                        accountNumber, true);
                bankResults.setAccountNo(accountNumber);
            } else {
                bankResults.setIbanNumber(accountNumber);
            }
            return Collections.singletonList(bankResults);
        }
        return null;
    }


}
