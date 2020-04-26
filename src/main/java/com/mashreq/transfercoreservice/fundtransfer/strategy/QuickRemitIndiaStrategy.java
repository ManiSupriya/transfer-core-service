package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;

import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils.generateBeneficiaryAddress;


@RequiredArgsConstructor
@Slf4j
@Service
public class QuickRemitIndiaStrategy implements QuickRemitFundTransfer {

    private static final String ORIGINATING_COUNTRY_ISO = "AE";
    private static final String INDIA_COUNTRY_ISO = "356";

    private final AccountService accountService;
    private final MobCommonService mobCommonService;


    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final LimitValidator limitValidator;
    private final QuickRemitFundTransferMWService quickRemitFundTransferMWService;
    private final MaintenanceService maintenanceService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO, ValidationContext validationContext) {
        log.info("Quick remit to INDIA starts");
        responseHandler(finTxnNoValidator.validate(request, metadata));

        final CustomerDetailsDto customerDetails = mobCommonService.getCustomerDetails(metadata.getPrimaryCif());

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final BeneficiaryDto beneficiaryDto = validationContext.get("beneficiary-dto", BeneficiaryDto.class);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(), beneficiaryDto.getBeneficiaryCountryISO());
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
        log.info("Quick Remit India middle-ware started");
        final FundTransferResponse fundTransferResponse = quickRemitFundTransferMWService.transfer(fundTransferRequest);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }


    /*private CurrencyConversionDto getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto,
                                                         AccountDetailsDTO sourceAccountDetailsDTO) {

        final CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccountDetailsDTO.getNumber())
                .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .transactionAmount(request.getAmount())
                .productCode(request.getProductCode())
                .build();
        return mobCommonService.getConvertBetweenCurrencies(currencyRequest);
    }*/

    private CurrencyConversionDto getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto,
                                                         AccountDetailsDTO sourceAccountDetailsDTO) {

        final CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccountDetailsDTO.getNumber())
                .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .productCode(request.getProductCode())
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
    /*private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccountDetailsDTO.getNumber())
                .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                .accountCurrencyAmount(transferAmountInSrcCurrency)
                .dealNumber(dealNumber)
                .transactionCurrency("AED")
                .build();
        CurrencyConversionDto currencyConversionDto = mobCommonService.getConvertBetweenCurrencies(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }*/

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
                .beneficiaryName(StringUtils.defaultIfBlank(beneficiaryDto.getFinalName(), beneficiaryDto.getFullName()))
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
                .senderCountryISOCode(customerDetails.getNationality())
                .senderBankBranch(customerDetails.getCifBranch())
                .senderMobileNo(CustomerDetailsUtils.getMobileNumber(customerDetails))
                .senderName(accountDetails.getCustomerName())
                .srcCurrency(accountDetails.getCurrency())
                //.srcISOCurrency("784") for AED
                .transactionAmount(request.getAmount().toString())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .build();

        return CustomerDetailsUtils.deriveSenderIdNumberAndAddress(quickRemitFundTransferRequest, customerDetails);

    }








}
