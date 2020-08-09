package com.mashreq.transfercoreservice.middleware;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.mashreq.esbcore.bindings.account.mbcdm.AccountReferenceResType;
import com.mashreq.esbcore.bindings.account.mbcdm.AccountSummaryResType;
import com.mashreq.esbcore.bindings.account.mbcdm.ComplianceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.AccountDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.ComplianceDto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class AccountDetailsResponseConverter implements Converter<AccountSummaryResType, AccountDetailsDto> {

    private AccountsSearchResponseConverter searchResponseConverter;

    @Override
    public AccountDetailsDto convert(AccountSummaryResType source) {
        AccountDetailsDto detailsDto = new AccountDetailsDto();
        detailsDto.setAlternativeAccount(source.getAltAccount());
        detailsDto.setChequeBookName(source.getChequeBookName1());
        detailsDto.setCustomerName(source.getCustomerName());
        detailsDto.setOnlineBanking(source.getOnlineBanking());
        detailsDto.setSalaryAccount(source.getSalaryAccount());
        detailsDto.setIban(source.getIBANAccountNumber());
        if (source.getAccReference() != null
                && !CollectionUtils.isEmpty(source.getAccReference().getAccountReference())) {
            detailsDto.setConnectedAccounts(searchResponseConverter.convert(source));
        } else {
            detailsDto.setConnectedAccounts(Collections.emptyList());
        }
        if(!detailsDto.getConnectedAccounts().isEmpty()){
            String chequeBook = detailsDto.getConnectedAccounts().get(0).getChequeBook();
            detailsDto.setChequeBookAllowed((chequeBook != null && chequeBook.equals("Y")));
        }
        detailsDto.setCompliances(mapCompliances(source.getAccReference()));
        return detailsDto;
    }

    private List<ComplianceDto> mapCompliances(AccountReferenceResType source) {
        return Optional.ofNullable(source)
                    .map(AccountReferenceResType::getCompliance)
                    .map(this::mapComplianceList)
                    .orElse(Collections.emptyList());
    }

    private List<ComplianceDto> mapComplianceList(List<ComplianceType> complianceTypes){
        return complianceTypes
            .stream()
            .map(this::complianceTypeToDto)
            .collect(Collectors.toList());
    }

    private ComplianceDto complianceTypeToDto(ComplianceType complianceType){
        ComplianceDto dto = new ComplianceDto();
        dto.setCode(complianceType.getReasonCode());
        dto.setDescription(complianceType.getReasonDescription());
        return dto;
    }
}