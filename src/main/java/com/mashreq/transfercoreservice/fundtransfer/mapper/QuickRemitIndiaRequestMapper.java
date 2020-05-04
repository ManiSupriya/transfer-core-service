package com.mashreq.transfercoreservice.fundtransfer.mapper;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils.generateBeneficiaryAddress;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuickRemitIndiaRequestMapper {

    private final SoapServiceProperties soapServiceProperties;

    private static final String INDIA_COUNTRY_ISO = "356";

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
                .serviceCode(soapServiceProperties.getServiceCodes().getQuickRemitIndia())
                .amountDESTCurrency(request.getAmount())
                .amountSRCCurrency(transferAmountInSrcCurrency)
                .beneficiaryAccountNo(beneficiaryDto.getAccountNumber())
                .beneficiaryAddress(generateBeneficiaryAddress(beneficiaryDto))
                .beneficiaryBankIFSC(beneficiaryDto.getRoutingCode())
                .beneficiaryBankName(beneficiaryDto.getBankName())
                .beneficiaryCountry(beneficiaryDto.getBankCountry())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .beneficiaryName(StringUtils.defaultIfBlank(beneficiaryDto.getFinalName(), beneficiaryDto.getFullName()))
                .channelTraceId(channelTraceId)
                .destCountry(beneficiaryDto.getBeneficiaryCountryISO())
                .destCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .srcISOCurrency(srcIsoCurrency)
                .destISOCurrency(INDIA_COUNTRY_ISO)
                .exchangeRate(exchangeRate)
                .finTxnNo(channelTraceId)
                .originatingCountry(srcIsoCountry)
                .reasonCode(request.getPurposeCode())
                .reasonText(request.getPurposeDesc())
                .senderBankAccount(accountDetails.getNumber())
                .senderCountryISOCode(customerDetails.getNationality())
                .senderBankBranch(customerDetails.getCifBranch())
                .senderMobileNo(CustomerDetailsUtils.getMobileNumber(customerDetails))
                .senderName(accountDetails.getCustomerName())
                .srcCurrency(accountDetails.getCurrency())
                .transactionAmount(request.getAmount().toString())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .beneficiaryMobileNo(beneficiaryDto.getMobileNumber())
                .build();

        return CustomerDetailsUtils.deriveSenderIdNumberAndAddress(quickRemitFundTransferRequest, customerDetails);

    }
}
