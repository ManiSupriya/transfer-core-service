package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.BankCodeUtils;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferContext.Constants.*;

/**
 * @author shahbazkh
 * @date 5/4/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class QuickRemitInstaRemRequestMapper implements QuickRemitMapper {

    private final SoapServiceProperties soapServiceProperties;


    @Override
    public QuickRemitFundTransferRequest mapTo(FundTransferMetadata metadata,
                                               FundTransferRequestDTO request,
                                               FundTransferContext fundTransferContext) {


        final BeneficiaryDto beneficiaryDto = fundTransferContext.get(BENEFICIARY_FUND_CONTEXT_KEY, BeneficiaryDto.class);
        final AccountDetailsDTO accountDetails = fundTransferContext.get(ACCOUNT_DETAILS_FUND_CONTEXT_KEY, AccountDetailsDTO.class);
        final CustomerDetailsDto customerDetails = fundTransferContext.get(CUSTOMER_DETAIL_FUND_CONTEXT_KEY, CustomerDetailsDto.class);
        final BigDecimal transferAmountInSrcCurrency = fundTransferContext.get(TRANSFER_AMOUNT_IN_SRC_CURRENCY_FUND_CONTEXT_KEY, BigDecimal.class);
        final BigDecimal exchangeRate = fundTransferContext.get(EXCHANGE_RATE_FUND_CONTEXT_KEY, BigDecimal.class);
        final String productCode = fundTransferContext.get(FLEX_PRODUCT_CODE_CONTEXT_KEY, String.class);


        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = QuickRemitFundTransferRequest.builder()
                .serviceCode(soapServiceProperties.getServiceCodes().getQuickRemitInstaRem())
                .finTxnNo(request.getFinTxnNo())
                .channelTraceId(metadata.getChannelTraceId())
                .productCode(productCode)
                .beneficiaryMobileNo(null)
                .beneficiaryName(beneficiaryDto.getFullName())
                .beneficiaryAddress(beneficiaryDto.getAddressLine1() + "," + beneficiaryDto.getAddressLine2() + "," + beneficiaryDto.getAddressLine3())
                .beneficiaryAccountNo(beneficiaryDto.getAccountNumber())
                .beneficiaryBankName(beneficiaryDto.getBankName())
                // Add city
                .beneficiaryCity(beneficiaryDto.getBeneficiaryCity())
                .beneficiaryState(beneficiaryDto.getBeneficiaryState())
                .beneficiaryPinCode(beneficiaryDto.getBeneficiaryPostalCode())
                .beneficiaryAccountType(WordUtils.capitalizeFully(beneficiaryDto.getBeneficiaryAccountType()))
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
                //TODO Hard code as Individual Pick from Property
                .senderAccountType("Individual")
                .senderMobileNo(CustomerDetailsUtils.getMobileNumber(customerDetails))
                .senderBankBranch(customerDetails.getCifBranch())
                .senderBankAccount(accountDetails.getNumber())
                .senderAddress(CustomerDetailsUtils.deriveAddress(customerDetails.getAddress()))

                //TODO Pick from Digital User Digital User
                .senderCountryISOCode("AE")

                .senderIDType(customerDetails.getUniqueIDName())
                .senderIDNumber(customerDetails.getUniqueIDValue())
                //TODO Hardcoded values check with Bala
                .senderCity("DUBAI")
                .senderState("DUBAI")
                .senderPostalCode("UAE")
                .senderBeneficiaryRelationship(beneficiaryDto.getBeneficiaryRelationship())
                //TODO When will these values be used and where to pick it from
                .senderDOB("1980-01-01")
                .beneficiaryIDNo(null)
                //TODO Check if destination country mapping is correct
                .destCountry(beneficiaryDto.getBeneficiaryCountryISO())
                .routingCode(BankCodeUtils.extractBankCodes(beneficiaryDto))
                .build();


        log.info("QuickRemitFundTransferRequest Object {} ", quickRemitFundTransferRequest);

        return quickRemitFundTransferRequest;
    }


}
