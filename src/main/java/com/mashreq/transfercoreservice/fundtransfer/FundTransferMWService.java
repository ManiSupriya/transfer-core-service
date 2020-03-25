package com.mashreq.transfercoreservice.fundtransfer;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.FundTransferResponse;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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


    public FundTransferResponse sendMoneyToIBAN(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                AccountDetailsDTO fromAccountDetails) {
        log.info("Fund transfer initiated for IBAN [ {} ]", request.getToAccount());

        EAIServices response = (EAIServices) webServiceClient.exchange(generateEAIRequest(metadata.getChannelTraceId(), request, fromAccountDetails));
        validateOMWResponse(response);
        final FundTransferResType.Transfer transfer = response.getBody().getFundTransferRes().getTransfer().get(0);
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();
        log.info("Fund transfer successful for IBAN [ {} ]", request.getToAccount());
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

    public EAIServices generateEAIRequest(String channelTranceId, FundTransferRequestDTO requestDTO, AccountDetailsDTO fromAccountDetails) {


        //TODO remove this
        SecureRandom secureRandom = new SecureRandom();
        int batchTransIdTemporatry = (int) (secureRandom.nextInt() * 9000) + 1000;
        String channelTraceIdTemporary = channelTranceId.substring(0, 12);
        String debitTraceIdTemporary = channelTranceId.substring(0, 15);


        EAIServices request = new EAIServices();
        request.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFundTransfer(), channelTraceIdTemporary));
        request.setBody(new EAIServices.Body());

        //Setting individual components
        FundTransferReqType fundTransferReqType = new FundTransferReqType();

        //TODO Change this to proper batch id
        fundTransferReqType.setBatchTransactionId(batchTransIdTemporatry + "");

        fundTransferReqType.setProductId("DBLC");
        fundTransferReqType.setTransTypeCode("FAM");

        List<FundTransferReqType.Transfer> transferList = fundTransferReqType.getTransfer();
        FundTransferReqType.Transfer.CreditLeg creditLeg = new FundTransferReqType.Transfer.CreditLeg();
        FundTransferReqType.Transfer.DebitLeg debitLeg = new FundTransferReqType.Transfer.DebitLeg();
        debitLeg.setDebitRefNo(debitTraceIdTemporary);
        debitLeg.setAccountNo("010490730773");
        debitLeg.setTransferBranch("005");
        debitLeg.setCurrency("AED");
        debitLeg.setNarration1("Fund Transfer-Mobile Banking");

        creditLeg.setAccountNo("AE120260001015673975601");
        creditLeg.setTransactionCode("015");
        creditLeg.setAmount(new BigDecimal(20.00));
        creditLeg.setCurrency("AED");
        creditLeg.setChargeBearer("O");
        creditLeg.setPaymentDetails("/REF/ Family Support");
        creditLeg.setBenName("Hasneet Singh Nehra");
        creditLeg.setBenAddr2("UNITED ARAB EMIRATES");
        creditLeg.setAWInstName("EMIRATES NBD PJSC");
        creditLeg.setAWInstBICCode("EBILAEADXXX");
        creditLeg.setAWInstAddr2("DUBAI");
        FundTransferReqType.Transfer transfer = new FundTransferReqType.Transfer();
        transfer.setCreditLeg(creditLeg);
        transfer.setDebitLeg(debitLeg);
        transferList.add(transfer);


        request.getBody().setFundTransferReq(fundTransferReqType);
        log.info("EAI Service request for fund transfer prepared {}", request);
        return request;
    }


}
