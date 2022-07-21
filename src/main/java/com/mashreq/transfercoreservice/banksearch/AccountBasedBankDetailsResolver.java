package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.common.LocalIbanValidator;
import com.mashreq.transfercoreservice.dto.BankResolverRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


import static com.mashreq.transfercoreservice.errors.ExceptionUtils.genericException;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_NOT_FOUND_WITH_IBAN;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_NOT_FOUND_FOR_BRANCH;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountBasedBankDetailsResolver implements BankDetailsResolver {

    private final IbanResolver ibanResolver;
    private final LocalIbanValidator localIbanValidator;

    private final IbanSearchMWService ibanSearchMWService;

    private final BankRepository bankRepository;

    @Override
    public List<BankResultsDto> getBankDetails(BankResolverRequestDto bankResolverRequestDto) {
        String iban = ibanResolver.constructIBAN(bankResolverRequestDto.getIdentifier(), bankResolverRequestDto.getBankCode(),
                bankResolverRequestDto.getBranchCode());
       return getBankResultForIban(bankResolverRequestDto.getJourneyType(), iban, bankResolverRequestDto.getRequestMetaData(),
               bankResolverRequestDto.getBranchCode());

    }

    private List<BankResultsDto> getBankResultForIban(String journeyType, String iban, RequestMetaData requestMetaData , String branchCode) {
        if("MT".equals(journeyType) &&
                localIbanValidator.isLocalIban(iban)) {
            return getLocalIbanBankDetails(iban, branchCode);
        }
        return ibanSearchMWService.fetchBankDetailsWithIban(requestMetaData.getChannelTraceId(), iban, requestMetaData );
    }

    private List<BankResultsDto> getLocalIbanBankDetails(String iban, String branchCode) {
        String bankCode = localIbanValidator.validate(iban);
        List<BankDetails> bankDetailsList = bankRepository.findByBankCode(bankCode).orElseThrow(() -> genericException(BANK_NOT_FOUND_WITH_IBAN));
        BankDetails bankDetail = bankDetailsList.stream().filter(bank -> bankCode.equals(bank.getBankCode()) && branchCode.equals(bank.getBranchCode())).findAny()
                .orElseThrow(() -> genericException(BANK_NOT_FOUND_FOR_BRANCH));

            BankResultsDto bankResults = new BankResultsDto();
            bankResults.setSwiftCode(bankDetail.getSwiftCode());
            bankResults.setBankName(bankDetail.getBankName());
            bankResults.setBranchName(bankDetail.getBranchName());
            String accountNo = localIbanValidator.extractAccountNumberIfMashreqIban(iban, bankCode);
            //If mashreq iban return account no otherwise iban
            if(StringUtils.isBlank(accountNo)){
                bankResults.setIbanNumber(iban);
            }else{
                bankResults.setAccountNo(accountNo);
            }
            return Collections.singletonList(bankResults);
    }
}
