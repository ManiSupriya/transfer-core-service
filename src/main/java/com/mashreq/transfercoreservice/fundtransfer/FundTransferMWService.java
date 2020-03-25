package com.mashreq.transfercoreservice.fundtransfer;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
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


    public CoreFundTransferResponseDto sendMoneyToIBAN(FundTransferMetadata metadata, FundTransferRequestDTO request) {
        log.info("Fund transfer initiated for IBAN [ {} ]", request.getToAccount());

        EAIServices response = (EAIServices) webServiceClient.exchange(generateEAIRequest(metadata.getChannelTraceId(), request));
        validateOMWResponse(response);
        final FundTransferResType.Transfer transfer = response.getBody().getFundTransferRes().getTransfer().get(0);
        final ErrorType exceptionDetails = response.getBody().getExceptionDetails();
        log.info("Fund transfer successful for IBAN [ {} ]", request.getToAccount());
        return CoreFundTransferResponseDto.builder()
                .transactionRefNo(transfer.getTransactionRefNo())
                .externalErrorMessage(exceptionDetails.getErrorDescription())
                .mwReferenceNo(exceptionDetails.getReferenceNo())
                .mwResponseDescription(exceptionDetails.getData())
                .mwResponseStatus(MwResponseStatus.S)
                .mwResponseCode(exceptionDetails.getErrorCode())
                .build();
    }

    private void validateOMWResponse(EAIServices response) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.debug("Exception during local fund transfer. Code: {} , Description: {}", response.getBody()
                    .getExceptionDetails().getErrorCode(),response.getBody().getExceptionDetails().getData());
            GenericExceptionHandler.handleError(TransferErrorCode.FUND_TRANSFER_FAILED,
                    response.getBody().getExceptionDetails().getErrorDescription());
        }
    }

    public EAIServices generateEAIRequest(String channelTranceId, FundTransferRequestDTO requestDTO) {
        EAIServices request = new EAIServices();
        request.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFundTransfer(), channelTranceId));
        request.setBody(new EAIServices.Body());

        //Setting individual components
        FundTransferReqType fundTransferReqType = new FundTransferReqType();
        fundTransferReqType.setBatchTransactionId("1587");
        fundTransferReqType.setPostingGroup("U");
        fundTransferReqType.setProductId("DBLC");
        fundTransferReqType.setTransTypeCode("FAM");

        List<FundTransferReqType.Transfer> transfer = fundTransferReqType.getTransfer();
        FundTransferReqType.Transfer.CreditLeg creditLeg = new FundTransferReqType.Transfer.CreditLeg();
        FundTransferReqType.Transfer.DebitLeg debitLeg = new FundTransferReqType.Transfer.DebitLeg();
        debitLeg.setDebitRefNo("A200218163859000");
        debitLeg.setAccountNo("010490730773");
        debitLeg.setTransferBranch("005");
        debitLeg.setCurrency("AED");
        debitLeg.setNarration1("Fund Transfer-Mobile Banking");

        creditLeg.setAccountNo("AE120260001015673975601");
        creditLeg.setTransactionCode("015");
        creditLeg.setAmount(new BigDecimal(200.00));
        creditLeg.setCurrency("AED");
        creditLeg.setChargeBearer("O");
        creditLeg.setPaymentDetails("/REF/ Family Support");
        creditLeg.setBenName("Hasneet Nehra");
        creditLeg.setBenAddr2("UNITED ARAB EMIRATES");
        creditLeg.setAWInstName("EMIRATES NBD PJSC");
        creditLeg.setAWInstBICCode("EBILAEADXXX");
        creditLeg.setAWInstAddr2("DUBAI");
        creditLeg.setAWInstAddr3("UNITED ARAB EMIRATES");

        transfer.get(0).setCreditLeg(creditLeg);
        transfer.get(0).setDebitLeg(debitLeg);

        request.getBody().setFundTransferReq(fundTransferReqType);
        log.info("EAI Service request for fund transfer prepared {}",request);
        return request;
    }


}
