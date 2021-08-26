package com.mashreq.transfercoreservice.banksearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashreq.esbcore.bindings.account.mbcdm.IBANDetailsResType;
import com.mashreq.esbcore.bindings.customer.mbcdm.AccuityDetailsTypes;
import com.mashreq.esbcore.bindings.customer.mbcdm.AxisRemittanceIFSCDetailsResType;

import com.mashreq.transfercoreservice.client.dto.BICCodeSearchResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author shahbazkh
 * @date 2/15/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankResultsDto {
    private String bankName;
    private String bankNameDb;
    private String bankCountry;
    private String bankState;
    private String bankCity;
    private String swiftCode;
    private String routingCode;
    private String routingType;
    private String ibanNumber;
    private String accountNo;
    private String swiftBankName;
    private String swiftBankCountry;
    private String swiftBankState;
    private String swiftBankCity;
    private String swiftBankBranch;
    private String pkAccuityLocId;
    private String hoAccLocid;
    private String rank;
    private String branchCode;
    private String branchName;
    private String countryCode;
    private String instTitleOver;
    private String postal;
    private String routingNo;
    private String street;
    private String swiftOver;
    private String bankCode;

    public BankResultsDto(AccuityDetailsTypes type) {
        setBankName(type.getBankName());
        setBankCountry(type.getBankCountry());
        setBankState(type.getBankState());
        setBankCity(type.getBankCity());
        setSwiftCode(type.getSwiftCode());
        setRoutingCode(type.getRoutingCode());
        setSwiftBankName(type.getSwiftBankName());
        setSwiftBankCountry(type.getSwiftBankCountry());
        setSwiftBankState(type.getSwiftBankState());
        setSwiftBankCity(type.getSwiftBankCity());
        setSwiftBankBranch(type.getSwiftBankBranch());
        setPkAccuityLocId(type.getPkAccuityLocId());
        setHoAccLocid(type.getHoAccLocid());
        setRank(type.getRank());
        setBranchName(type.getBankBranch());
    }

    public BankResultsDto(IBANDetailsResType ibanDetailsRes , List<String> rountingCodeEnabledCountryCodes) {
        final boolean isRoutingCountry = rountingCodeEnabledCountryCodes
                .stream()
                .anyMatch(cntrycode -> StringUtils.equals(cntrycode, ibanDetailsRes.getCountryCode()));
        log.info("[BankResultsDto] IBAN Search: Is search country routing code enabled : {} , country code: {}",
                isRoutingCountry, ibanDetailsRes.getCountryCode());
        setAccountNo(ibanDetailsRes.getAccountNo());
        setBranchCode(ibanDetailsRes.getBranchCode());
        setBranchName(ibanDetailsRes.getBranchName());
        setCountryCode(ibanDetailsRes.getCountryCode());
        setIbanNumber(ibanDetailsRes.getIBAN());
        setBankCity(ibanDetailsRes.getPostal());
        setBankName(ibanDetailsRes.getInstTitle());
        setInstTitleOver(ibanDetailsRes.getInstTitleOver());
        setStreet(ibanDetailsRes.getStreet());
        setSwiftCode(ibanDetailsRes.getSwiftBic());
        setSwiftOver(ibanDetailsRes.getSwiftOver());
        if(isRoutingCountry) {
            log.info("[BankResultsDto] setting routing code {} for routing enabled country {} ",ibanDetailsRes.getRoutingNO());
            setRoutingNo(ibanDetailsRes.getRoutingNO());
        }
    }


    public BankResultsDto(AxisRemittanceIFSCDetailsResType s) {
        setCountryCode(s.getIFSCDetails().getCountry());
        setBankName(s.getIFSCDetails().getBankName());
        setBankState(s.getIFSCDetails().getState());
        setBankCity(s.getIFSCDetails().getCity());
        setBranchName(s.getIFSCDetails().getBranchName());

    }

    public BankResultsDto(BICCodeSearchResponseDto bankDetails) {
        setBankName(bankDetails.getBankName());
        setBankCountry(bankDetails.getBankCountry());
        //change after discussing with Nava and Indrajit as branchCode is mapped to Address3 in SME
        setBankCity(bankDetails.getBranchCode());
        setSwiftCode(bankDetails.getSwiftCode());
        //setBranchCode(bankDetails.getBankCity());
        setBranchName(bankDetails.getBranchName());
    }
}
