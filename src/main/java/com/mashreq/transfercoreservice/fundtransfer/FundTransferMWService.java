package com.mashreq.transfercoreservice.fundtransfer;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private static final String NARRATION_PREFIX = "Fund Transfer-";
    private static final String NARRATION_SUFFIX = " Banking";
    private static final String PAYMENT_DETAIL_PREFIX = "/REF/ ";

    @Value("${app.uae.address}")
    private String address;

    @Value("${app.local.transfer.product.id}")
    private String productId;

    @Value("${app.local.transaction.code}")
    private String transactionCode;

    @Value("${app.local.currency}")
    private String localCurrency;

    public FundTransferResponse sendMoneyToIBAN(FundTransferRequest request) {
        log.info("Fund transfer initiated for IBAN [ {} ]", request.getFundTransferRequestDTO().getToAccount());

        EAIServices response = (EAIServices) webServiceClient.exchange(generateEAIRequest(request));

        validateOMWResponse(response);
        final FundTransferResType.Transfer transfer = response.getBody().getFundTransferRes().getTransfer().get(0);
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();
        log.info("Fund transfer successful for IBAN [ {} ]", request.getFundTransferRequestDTO().getToAccount());
        final CoreFundTransferResponseDto coreFundTransferResponseDto = CoreFundTransferResponseDto.builder()
                .transactionRefNo(transfer.getTransactionRefNo())
                .externalErrorMessage(exceptionDetails.getErrorDescription())
                .mwReferenceNo(exceptionDetails.getReferenceNo())
                .mwResponseDescription(exceptionDetails.getData())
                .mwResponseStatus(MwResponseStatus.S)
                .mwResponseCode(exceptionDetails.getErrorCode())
                .build();
        return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
    }

    private void validateOMWResponse(EAIServices response) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.debug("Exception during local fund transfer. Code: {} , Description: {}", response.getBody()
                    .getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getData());
            GenericExceptionHandler.handleError(TransferErrorCode.FUND_TRANSFER_FAILED,
                    response.getBody().getExceptionDetails().getErrorDescription());
        }
    }

    public EAIServices generateEAIRequest(FundTransferRequest request) {
        final FundTransferMetadata fundTransferMetadata = request.getFundTransferMetadata();
        final FundTransferRequestDTO requestDTO = request.getFundTransferRequestDTO();
        final AccountDetailsDTO fromAccountDetails = request.getAccountDetailsDTO();
        final BeneficiaryDto beneficiaryDto = request.getBeneficiaryDto();
        //TODO remove this
        SecureRandom secureRandom = new SecureRandom();
        int batchTransIdTemporatry = Math.abs((secureRandom.nextInt() * 9000) + 1000);
        //String channelTraceIdTemporary = fundTransferMetadata.getChannelTraceId().substring(0, 12);
        //String debitTraceIdTemporary = fundTransferMetadata.getChannelTraceId().substring(0, 15);

        EAIServices services = new EAIServices();
        services.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFundTransfer(), channelTraceIdTemporary));
        services.setBody(new EAIServices.Body());


        //Setting individual components
        FundTransferReqType fundTransferReqType = new FundTransferReqType();

        //TODO Change this to proper batch id
        fundTransferReqType.setBatchTransactionId(batchTransIdTemporary + "");

        fundTransferReqType.setProductId(productId);
        fundTransferReqType.setTransTypeCode(requestDTO.getPurposeCode());

        List<FundTransferReqType.Transfer> transferList = fundTransferReqType.getTransfer();
        FundTransferReqType.Transfer.CreditLeg creditLeg = new FundTransferReqType.Transfer.CreditLeg();
        FundTransferReqType.Transfer.DebitLeg debitLeg = new FundTransferReqType.Transfer.DebitLeg();

        debitLeg.setDebitRefNo(debitTraceIdTemporary);
        debitLeg.setAccountNo(fromAccountDetails.getNumber());
        debitLeg.setTransferBranch(fromAccountDetails.getBranchCode());
        debitLeg.setCurrency(fromAccountDetails.getCurrency());
        debitLeg.setNarration1(generateNarration(fundTransferMetadata.getChannel()));

        creditLeg.setAccountNo(requestDTO.getToAccount());
        creditLeg.setTransactionCode(transactionCode);
        creditLeg.setAmount(requestDTO.getAmount());
        creditLeg.setCurrency(localCurrency);
        creditLeg.setChargeBearer(requestDTO.getChargeBearer());
        creditLeg.setPaymentDetails(PAYMENT_DETAIL_PREFIX + requestDTO.getPurposeDesc());
        creditLeg.setBenName(beneficiaryDto.getFullName());
        creditLeg.setBenAddr2(address);
        creditLeg.setAWInstName(beneficiaryDto.getBankName());
        creditLeg.setAWInstBICCode(beneficiaryDto.getSwiftCode());


        FundTransferReqType.Transfer transfer = new FundTransferReqType.Transfer();
        transfer.setCreditLeg(creditLeg);
        transfer.setDebitLeg(debitLeg);
        transferList.add(transfer);


        services.getBody().setFundTransferReq(fundTransferReqType);
        log.info("EAI Service request for fund transfer prepared {}", services);
        return services;
    }

    private String generateNarration(String channel) {
        return NARRATION_PREFIX + channel + NARRATION_SUFFIX;
    }




}
