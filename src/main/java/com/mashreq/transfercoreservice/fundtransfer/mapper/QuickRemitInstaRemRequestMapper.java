package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author shahbazkh
 * @date 5/4/20
 */

@Service
public class QuickRemitInstaRemRequestMapper {

    public QuickRemitFundTransferRequest map(FundTransferMetadata metadata,
                                             FundTransferRequestDTO request,
                                             FundTransferContext fundTransferContext) {


        BeneficiaryDto beneficiaryDto = fundTransferContext.get("beneficiary-dto", BeneficiaryDto.class);
        AccountDetailsDTO accountDetails = fundTransferContext.get("account-details-dto", AccountDetailsDTO.class);
        CustomerDetailsDto customerDetails = fundTransferContext.get("customer-detail-dto", CustomerDetailsDto.class);
        BigDecimal transferAmountInSrcCurrency = fundTransferContext.get("transfer-amount-in-src-currency", BigDecimal.class);
        BigDecimal exchangeRate = fundTransferContext.get("exchange-rate", BigDecimal.class);


        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = QuickRemitFundTransferRequest.builder()
                .finTxnNo(request.getFinTxnNo())
                .channelTraceId(metadata.getChannelTraceId())
                .productCode(request.getProductCode())
                .beneficiaryMobileNo(null)
                .beneficiaryName(beneficiaryDto.getFullName())
                .beneficiaryAddress(beneficiaryDto.getAddressLine1() + "," + beneficiaryDto.getAddressLine2() + "," + beneficiaryDto.getAddressLine3())
                .beneficiaryAccountNo(beneficiaryDto.getAccountNumber())
                .beneficiaryBankName(beneficiaryDto.getBankName())
                .beneficiaryCity(beneficiaryDto.getCity())
                .beneficiaryState(beneficiaryDto.getState())
                .beneficiaryPinCode(beneficiaryDto.getPingCode())
                .beneficiaryBankAccountType(beneficiaryDto.getBankAccountType())
                .amountSRCCurrency(transferAmountInSrcCurrency)
                .amountDESTCurrency(request.getAmount())
                .srcCurrency(accountDetails.getCurrency())
                .destCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .exchangeRate(exchangeRate)
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .paymentNarration(request.getPurposeDesc())
                .reasonCode(request.getPurposeCode())
                .reasonText(request.getPurposeDesc())
                .senderName(accountDetails.getCustomerName())
                //TODO Hard code as individual
                .senderAccountType("INDIVIDUAL")
                .senderMobileNo(CustomerDetailsUtils.getMobileNumber(customerDetails))
                .senderBankBranch(customerDetails.getCifBranch())
                .senderBankAccount(accountDetails.getNumber())
                .senderAddress(CustomerDetailsUtils.deriveAddress(customerDetails.getAddress()))

                //TODO Pick from Digital User Digital User
                .senderCountryISOCode(customerDetails.getNationality())

                .senderIDType(customerDetails.getUniqueIDName())
                .senderIDNumber(customerDetails.getUniqueIDValue())
                //TODO Hardcoded values check with Bala
                .senderCity("DUBAI")
                .senderState("DUBAI")
                .senderPostalCode("UAE")
                .senderBeneficiaryRelationship(beneficiaryDto.getBeneficiaryRelationship())
                //TODO When will these values be used and where to pick it from
                .senderDOB(null)
                .beneficiaryIDNo(null)
                //TODO Check if destination country mapping is correct
                .destCountry(beneficiaryDto.getBankCountry())
                //TODO This mappings needs to be completed by generating with new XSD
                .routingCode(Arrays.asList(new RoutingCode("SWIFT", "EVBLNPKAXXX")))
                .build();

        return quickRemitFundTransferRequest;
    }


}
