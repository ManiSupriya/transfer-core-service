package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.CustomerService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class QuickRemitIndiaStrategy implements QuickRemitFundTransfer {

    private static final String ORIGINATING_COUNTRY_ISO = "AE";
    private static final String INDIA_COUNTRY_ISO = "356";
    private static final String COMMA = ",";

    private final AccountService accountService;
    private final MobCommonService mobCommonService;
    private final MaintenanceService maintenanceService;
    private final CustomerService customerService;

    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final LimitValidator limitValidator;
    private final QuickRemitFundTransferMWService quickRemitFundTransferMWService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO, ValidationContext validationContext) {
        log.info("Quick remit to INDIA initiated......");
        responseHandler(finTxnNoValidator.validate(request, metadata));

        final CustomerDetailsDto customerDetails = customerService.getCustomerDetails(metadata.getPrimaryCif());

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final BeneficiaryDto beneficiaryDto = validationContext.get("beneficiary-dto", BeneficiaryDto.class);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(metadata
                .getChannelTraceId(), request.getServiceType(), beneficiaryDto.getBeneficiaryCountryISO());
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));

        //Balance Validation
        final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("to-account-currency", beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);

        final CurrencyConversionDto currencyConversionDto = getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);
        validationContext.add("transfer-amount-in-source-currency", currencyConversionDto.getAccountCurrencyAmount());
        responseHandler(balanceValidator.validate(request, metadata, validationContext));

        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,
                currencyConversionDto.getAccountCurrencyAmount());
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);


        final QuickRemitFundTransferRequest fundTransferRequest = prepareQuickRemitFundTransferRequestPayload(metadata.getChannelTraceId(),
                request, sourceAccountDetailsDTO, beneficiaryDto, currencyConversionDto.getAccountCurrencyAmount(),
                currencyConversionDto.getExchangeRate(), customerDetails);
        log.info("International Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = quickRemitFundTransferMWService.transfer(fundTransferRequest);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private boolean isCurrencySame(BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        return sourceAccountDetailsDTO.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency());
    }

    private CurrencyConversionDto getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto,
                                                         AccountDetailsDTO sourceAccountDetailsDTO) {

        final CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccountDetailsDTO.getNumber())
                .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .transactionAmount(request.getAmount()).build();
        return maintenanceService.convertBetweenCurrencies(currencyRequest);
    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
        return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccountDetailsDTO.getNumber())
                .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                .accountCurrencyAmount(transferAmountInSrcCurrency)
                .dealNumber(dealNumber)
                .transactionCurrency("AED")
                .build();

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private QuickRemitFundTransferRequest prepareQuickRemitFundTransferRequestPayload(String channelTraceId,
                                                                                      FundTransferRequestDTO request,
                                                                                      AccountDetailsDTO accountDetails,
                                                                                      BeneficiaryDto beneficiaryDto,
                                                                                      BigDecimal transferAmountInSrcCurrency,
                                                                                      BigDecimal exchangeRate,
                                                                                      CustomerDetailsDto customerDetails) {


        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = QuickRemitFundTransferRequest.builder()
                .amountDESTCurrency(request.getAmount())
                .amountSRCCurrency(transferAmountInSrcCurrency)
                .beneficiaryAccountNo(beneficiaryDto.getAccountNumber())
                .beneficiaryAddress(generateBeneficiaryAddress(beneficiaryDto))
                .beneficiaryBankIFSC(beneficiaryDto.getRoutingCode())
                .beneficiaryBankName(beneficiaryDto.getBankName())
                .beneficiaryCountry(beneficiaryDto.getBankCountry())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .beneficiaryName(beneficiaryDto.getFinalName())
                .channelTraceId(channelTraceId)
                .destCountry(beneficiaryDto.getBeneficiaryCountryISO())
                .destCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .destISOCurrency(INDIA_COUNTRY_ISO)
                .exchangeRate(exchangeRate)
                .finTxnNo(request.getFinTxnNo())
                .originatingCountry(ORIGINATING_COUNTRY_ISO)
                .reasonCode(request.getPurposeCode())
                .reasonText(request.getPurposeDesc())
                .senderBankAccount(accountDetails.getNumber())
                .senderCountryISOCode(customerDetails.getResidenceCountry())
                .senderBankBranch(customerDetails.getBranchName())
                .senderMobileNo(getMobileNumber(customerDetails))
                .senderName(accountDetails.getCustomerName())
                .srcCurrency(accountDetails.getCurrency())
                //.srcISOCurrency("784") for AED
                .transactionAmount(request.getAmount().toString())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .build();

        return deriveSenderIdNumberAndAddress(quickRemitFundTransferRequest, customerDetails);

    }


    private String getMobileNumber(CustomerDetailsDto customerDetails) {
        return StringUtils.defaultIfBlank(customerDetails.getMobile(), customerDetails.getPrimaryPhoneNumber());
    }

    private QuickRemitFundTransferRequest deriveSenderIdNumberAndAddress(QuickRemitFundTransferRequest remitFundTransferRequest, CustomerDetailsDto customerDetails) {
        String address = deriveAddress(customerDetails.getAddress());

        if (StringUtils.isNotBlank(customerDetails.getNationalNumber())) {
            return remitFundTransferRequest.toBuilder().senderIDType("NATIONAL ID")
                    .senderIDNumber(customerDetails.getNationalNumber()).senderAddress(address).build();
        } else if (StringUtils.isNotBlank(customerDetails.getPassportNumber())) {
            return remitFundTransferRequest.toBuilder().senderIDType("PASSPORT ID")
                    .senderIDNumber(customerDetails.getPassportNumber()).senderAddress(address).build();
        } else {
            return remitFundTransferRequest.toBuilder().senderIDType("VISA ID")
                    .senderIDNumber(customerDetails.getVisaNumber()).senderAddress(address).build();
        }
    }

    private String deriveAddress(List<AddressTypeDto> address) {
        List<String> types = Arrays.asList("P", "R");
        final Optional<AddressTypeDto> first = address.stream().filter(a -> types.contains(a.getAddressType())).findFirst();
        return first.map(this::generateSenderAddress).orElse("");
    }

    private String generateSenderAddress(AddressTypeDto addressTypeDto) {
        return StringUtils.defaultIfBlank(addressTypeDto.getAddress1(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress2(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress3(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress4(), "") + COMMA +
                StringUtils.defaultIfBlank(addressTypeDto.getAddress5(), "") + COMMA;
    }

    private String generateBeneficiaryAddress(BeneficiaryDto beneficiaryDto) {
        return StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine1(), "") + COMMA +
                StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine2(), "") + COMMA +
                StringUtils.defaultIfBlank(beneficiaryDto.getAddressLine3(), "");
    }
}
