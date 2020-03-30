package com.mashreq.transfercoreservice.fundtransfer;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
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
        log.info("Fund transfer initiated for IBAN [ {} ]", request.getToAccount());

        EAIServices response = (EAIServices) webServiceClient.exchange(generateEAIRequest(request));

        final FundTransferResType.Transfer transfer = response.getBody().getFundTransferRes().getTransfer().get(0);
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();
        if (isSuccessfull(response)) {
            log.info("Fund transfer successful for IBAN [ {} ]", request.getToAccount());
            final CoreFundTransferResponseDto coreFundTransferResponseDto = constructFTResponseDTO(transfer, exceptionDetails, MwResponseStatus.S);
            return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
        }

        log.info("Fund transfer failed for IBAN [ {} ]", request.getToAccount());
        final CoreFundTransferResponseDto coreFundTransferResponseDto = constructFTResponseDTO(transfer, exceptionDetails, MwResponseStatus.F);
        return FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
    }

    private CoreFundTransferResponseDto constructFTResponseDTO(FundTransferResType.Transfer transfer, ErrorType exceptionDetails, MwResponseStatus s) {
        return CoreFundTransferResponseDto.builder()
                .transactionRefNo(transfer.getTransactionRefNo())
                .externalErrorMessage(exceptionDetails.getData())
                .mwReferenceNo(transfer.getTransactionRefNo())
                .mwResponseDescription(exceptionDetails.getErrorDescription())
                .mwResponseStatus(s)
                .mwResponseCode(exceptionDetails.getErrorCode())
                .build();
    }

    private boolean isSuccessfull(EAIServices response) {
        log.info("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.error("Exception during local fund transfer. Code: {} , Description: {}", response.getBody()
                    .getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getData());

            return false;
        }
        return true;
    }

    public EAIServices generateEAIRequest(FundTransferRequest request) {

        //TODO remove this
        SecureRandom secureRandom = new SecureRandom();
        int batchTransIdTemporary = Math.abs((secureRandom.nextInt() * 9000) + 1000);

        EAIServices services = new EAIServices();
        services.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFundTransfer(), request.getChannelTraceId()));
        services.setBody(new EAIServices.Body());


        //Setting individual components
        FundTransferReqType fundTransferReqType = new FundTransferReqType();

        //TODO Change this to proper batch id
        fundTransferReqType.setBatchTransactionId(batchTransIdTemporary + "");

        fundTransferReqType.setProductId(productId);
        fundTransferReqType.setTransTypeCode(request.getPurposeCode());

        List<FundTransferReqType.Transfer> transferList = fundTransferReqType.getTransfer();
        FundTransferReqType.Transfer.CreditLeg creditLeg = new FundTransferReqType.Transfer.CreditLeg();
        FundTransferReqType.Transfer.DebitLeg debitLeg = new FundTransferReqType.Transfer.DebitLeg();

        debitLeg.setDebitRefNo(request.getFinTxnNo());
        debitLeg.setAccountNo(request.getFromAccount());
        debitLeg.setTransferBranch(request.getSourceBranchCode());
        debitLeg.setCurrency(request.getSourceCurrency());
        debitLeg.setNarration1(generateNarration(request.getChannel()));

        creditLeg.setAccountNo(request.getToAccount());
        creditLeg.setTransactionCode(transactionCode);
        creditLeg.setAmount(request.getAmount());
        creditLeg.setCurrency(localCurrency);
        creditLeg.setChargeBearer(request.getChargeBearer());
        creditLeg.setPaymentDetails(PAYMENT_DETAIL_PREFIX + request.getPurposeDesc());
        creditLeg.setBenName(request.getBeneficiaryFullName());
        creditLeg.setBenAddr2(address);
        creditLeg.setAWInstName(request.getDestinationBankName());
        creditLeg.setAWInstBICCode(request.getSwiftCode());


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
