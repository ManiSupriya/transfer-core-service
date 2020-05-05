package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.RoutingCode;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author shahbazkh
 * @date 5/4/20
 */
public class QuickRemitInstaRemRequestMapper {

    public QuickRemitFundTransferRequest map(String channelTraceId,
                                             FundTransferRequestDTO request,
                                             AccountDetailsDTO accountDetails,
                                             BeneficiaryDto beneficiaryDto,
                                             BigDecimal transferAmountInSrcCurrency,
                                             BigDecimal exchangeRate,
                                             ValidationContext validationContext,
                                             CustomerDetailsDto customerDetails) {

        final String srcIsoCurrency = validationContext.get("src-currency-iso", String.class);
        final String srcIsoCountry = validationContext.get("src-country-iso", String.class);

        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = QuickRemitFundTransferRequest.builder()
                .finTxnNo(request.getFinTxnNo())
                .channelTraceId(channelTraceId)
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
                .senderBeneficiaryRelationship("SIBLING")
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
