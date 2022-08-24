package com.mashreq.transfercoreservice.banksearch;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.common.LocalIbanValidator;
import com.mashreq.transfercoreservice.dto.BankResolverRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.mashreq.transfercoreservice.errors.ExceptionUtils.genericException;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_NOT_FOUND_WITH_IBAN;
@Service
@RequiredArgsConstructor
@Slf4j
public class IbanBasedBankDetailsResolver implements BankDetailsResolver {

    private final LocalIbanValidator localIbanValidator;

    private final IbanSearchMWService ibanSearchMWService;

    private final BankRepository bankRepository;

    @Override
    public List<BankResultsDto> getBankDetails(BankResolverRequestDto bankResolverRequestDto) {
        return getBankResultForIban(bankResolverRequestDto.getJourneyType(), bankResolverRequestDto.getIdentifier(), bankResolverRequestDto.getRequestMetaData());

    }

    private List<BankResultsDto> getBankResultForIban(String journeyType, String iban, RequestMetaData requestMetaData ) {
        if("MT".equals(journeyType) &&
                localIbanValidator.isLocalIban(iban)) {
            return getLocalIbanBankDetails(iban);
        }
        return ibanSearchMWService.fetchBankDetailsWithIban(requestMetaData.getChannelTraceId(), iban, requestMetaData );
    }

    private List<BankResultsDto> getLocalIbanBankDetails(String iban) {
        String bankCode = localIbanValidator.validate(iban);
        List<BankDetails> bankDetailsList = bankRepository.findByBankCode(bankCode).orElseThrow(() -> genericException(BANK_NOT_FOUND_WITH_IBAN));

        // We can extract branch code from iban and set the branch name here(only for Egypt). Is it needed?
            BankResultsDto bankResults = new BankResultsDto();
            bankResults.setSwiftCode(bankDetailsList.get(0).getSwiftCode());
            bankResults.setBankName(bankDetailsList.get(0).getBankName());
            bankResults.setAccountNo(localIbanValidator.extractAccountNumberIfMashreqIban(iban, bankCode));
            bankResults.setIbanNumber(iban);
            bankResults.setIdentifierType(BankCodeType.IBAN.getName());
            return Collections.singletonList(bankResults);

    }



}
