package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitFundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.mapper.QuickRemitPakistanRequestMapper;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
@Slf4j
@Service
public class QuickRemitPakistanStrategy implements QuickRemitFundTransfer {

    private static final String INDIVIDUAL_ACCOUNT = "I";

    private final AccountService accountService;
    private final MobCommonService mobCommonService;
    private final MaintenanceService maintenanceService;
    private final QuickRemitFundTransferMWService quickRemitFundTransferMWService;

    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final LimitValidator limitValidator;

    private final QuickRemitPakistanRequestMapper quickRemitPakistanRequestMapper;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO, ValidationContext validationContext) {
        log.info("Quick remit to PAKISTAN starts");
        responseHandler(finTxnNoValidator.validate(request, metadata));

        final CustomerDetailsDto customerDetails = mobCommonService.getCustomerDetails(metadata.getPrimaryCif());

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final BeneficiaryDto beneficiaryDto = validationContext.get("beneficiary-dto", BeneficiaryDto.class);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(),
                beneficiaryDto.getBeneficiaryCountryISO(), INDIVIDUAL_ACCOUNT);
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


        final QuickRemitFundTransferRequest fundTransferRequest = quickRemitPakistanRequestMapper.map(metadata.getChannelTraceId(),
                request, sourceAccountDetailsDTO, beneficiaryDto, currencyConversionDto.getAccountCurrencyAmount(),
                currencyConversionDto.getExchangeRate(), validationContext, customerDetails);
        log.info("Quick Remit India middle-ware started");
        final FundTransferResponse fundTransferResponse = quickRemitFundTransferMWService.transfer(fundTransferRequest, metadata);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();


    }

    private CurrencyConversionDto getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto,
                                                         AccountDetailsDTO sourceAccountDetailsDTO) {

        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency(beneficiaryDto.getBeneficiaryCurrency());
        currencyRequest.setProductCode(request.getProductCode());
        currencyRequest.setTransactionAmount(request.getAmount());

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
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        		currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        		currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        		currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        		currencyConversionRequestDto.setDealNumber(dealNumber);
        		currencyConversionRequestDto.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }




}
