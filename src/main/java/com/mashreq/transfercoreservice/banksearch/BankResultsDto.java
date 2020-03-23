package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.esbcore.bindings.account.mbcdm.IBANDetailsResType;
import com.mashreq.esbcore.bindings.customer.mbcdm.AccuityDetailsTypes;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 2/15/20
 */
@Data
public class BankResultsDto {
    private String bankName;
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

    public BankResultsDto(AccuityDetailsTypes type){
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

    public BankResultsDto(IBANDetailsResType ibanDetailsRes) {
        setAccountNo(ibanDetailsRes.getAccountNo());
        setBranchCode(ibanDetailsRes.getBranchCode());
        setBranchName(ibanDetailsRes.getBranchName());
        setCountryCode(ibanDetailsRes.getCountryCode());
        setIbanNumber(ibanDetailsRes.getIBAN());
        setBankCity(ibanDetailsRes.getPostal());
        setBankName(ibanDetailsRes.getInstTitle());
        setInstTitleOver(ibanDetailsRes.getInstTitleOver());
        setRoutingNo(ibanDetailsRes.getRoutingNO());
        setStreet(ibanDetailsRes.getStreet());
        setSwiftCode(ibanDetailsRes.getSwiftBic());
        setSwiftOver(ibanDetailsRes.getSwiftOver());
    }
}
