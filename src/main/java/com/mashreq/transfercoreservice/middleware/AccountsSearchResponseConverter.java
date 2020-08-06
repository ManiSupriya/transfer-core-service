package com.mashreq.transfercoreservice.middleware;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


import com.mashreq.esbcore.bindings.account.mbcdm.AccountReference;
import com.mashreq.esbcore.bindings.account.mbcdm.AccountSummaryResType;
import com.mashreq.esbcore.bindings.account.mbcdm.BalType;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountTypeDto;
import com.mashreq.transfercoreservice.middleware.enums.OperatingInstruction;
import com.mashreq.transfercoreservice.middleware.enums.YesNo;

@Component
public class AccountsSearchResponseConverter implements Converter<AccountSummaryResType, List<SearchAccountDto>> {

    public static final List<String> requiredFields
            = Collections.unmodifiableList(Arrays.asList("accReference.accountReference"));
    private static final String PRIORITY_STATUS = "ACTIVE";
    public static final String ACY_UNCOLLECTED = "ACY_UNCOLLECTED";

    @Override
    public List<SearchAccountDto> convert(AccountSummaryResType source) {
        return source.getAccReference().getAccountReference().stream().map(accRef -> {
            SearchAccountDto searchAccountDto = new SearchAccountDto();
            searchAccountDto.setAccountType(new SearchAccountTypeDto(accRef.getType().getAccType(),
                    accRef.getType().getSchmType()));
            searchAccountDto.setCustomerName(accRef.getCustomerName());
            searchAccountDto.setOverdraftStartDate(Objects.isNull(accRef.getOverdraftStartDate()) ? null
                    : accRef.getOverdraftStartDate().toString());
            searchAccountDto.setOverdraftExpiryDate(Objects.isNull(accRef.getOverdraftExpiryDate()) ? null
                    : accRef.getOverdraftExpiryDate().toString());
            searchAccountDto.setTempOverDraft(Objects.isNull(accRef.getTempOverDraft()) ? null
                    : accRef.getTempOverDraft().toString());
            searchAccountDto.setNoDebitForCompliance(YesNo.Y.name().equalsIgnoreCase(accRef.getNoDebitKYCExpiry()));
            searchAccountDto.setOverDraft(accRef.getOverDraft());
            searchAccountDto.setChequeBook(accRef.getChequeBook());
            searchAccountDto.setCustomerCif(accRef.getCIFId());
            searchAccountDto.setAccountName(accRef.getAccountShortName());
            searchAccountDto.setCurrency(accRef.getAccountCurrency());
            searchAccountDto.setNumber(accRef.getAccountNo());
            if ("CLOSED".equalsIgnoreCase(accRef.getStatus().getStatus())) {
                searchAccountDto.setAccountClosedDate(source.getCheckerStamp());
            }
            searchAccountDto.setCreationDate(Objects.isNull(accRef.getCreationDate()) ? null
                    : accRef.getCreationDate().toString());
            searchAccountDto.setOperatingInstruction(accRef.getOperatingInstruction());
            searchAccountDto.setJointAccount(OperatingInstruction.J
                    == OperatingInstruction.from(accRef.getOperatingInstruction()));
            searchAccountDto.setBranch(accRef.getAccountBranch());
            searchAccountDto.setNoDebitForCompliance(YesNo.from(accRef.getNoDebitKYCExpiry(), YesNo.N).val());
            searchAccountDto.setNoDebit(YesNo.from(accRef.getNoOfDebits(), YesNo.N).val());
            searchAccountDto.setNoCredit(YesNo.from(accRef.getNoOfCredits(), YesNo.N).val());
            final BalType balance = accRef.getBalance();
            if (Objects.nonNull(balance)) {
                if (balance.getCurrentBal() != null) {
                    searchAccountDto.setCurrentBalance(balance.getCurrentBal().toPlainString());
                }
                if (balance.getAvailableBalance() != null) {
                    searchAccountDto.setAvailableBalance(balance.getAvailableBalance().toPlainString());
                }
                if (balance.getTotalOverdraft() != null) {
                    searchAccountDto.setTotalOverdraft(balance.getTotalOverdraft().toPlainString());
                }
                if (balance.getHoldAmount() != null) {
                    searchAccountDto.setHoldAmount(balance.getHoldAmount().toPlainString());
                }
                if (balance.getFreezeAmount() != null) {
                    searchAccountDto.setFreezeAmount(balance.getFreezeAmount().toPlainString());
                }
            }
            if (Objects.nonNull(accRef.getStatus())) {
                searchAccountDto.setStatus(accRef.getStatus().getStatus());
                searchAccountDto.setDormant(YesNo.from(accRef.getStatus().getIsDormant(), YesNo.N).val());
                searchAccountDto.setFrozen(YesNo.from(accRef.getStatus().getIsBlocked(), YesNo.N).val());
                searchAccountDto.setClosed("O".equalsIgnoreCase(accRef.getStatus().getIsClosed()) ? YesNo.N.val() : YesNo.Y.val());
            }
            searchAccountDto.setUnclearedBalance(getUnclearedBalance(accRef));


            return searchAccountDto;
        })
                .sorted(Comparator.comparing(SearchAccountDto::getStatus, new SortByValueFirst<>(PRIORITY_STATUS)))
                .collect(Collectors.toList());
    }

    private String getUnclearedBalance(AccountReference accRef) {
        if (CollectionUtils.isEmpty(accRef.getFields()))
            return null;

        return accRef.getFields().stream()
                .filter(x -> ACY_UNCOLLECTED.equals(x.getName()))
                .map(x -> x.getValue())
                .findAny()
                .orElse(null);
    }
}