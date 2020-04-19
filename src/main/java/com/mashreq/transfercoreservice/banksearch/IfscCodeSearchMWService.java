package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.esbcore.bindings.customer.mbcdm.AxisRemittanceIFSCDetailsReqType;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.axisremittanceifscdetails.EAIServices;

/**
 * @author shahbazkh
 * @date 4/19/20
 * <p>
 * This class will be used to search bank details based on IFSC code for Quick Remit Journey
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class IfscCodeSearchMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";

    public BankResultsDto getBankDetailByIfscCode(final String channelTraceId, final String ifscCode) {
        log.info("Searching for Bank details with ifsc-cde [ {} ]", ifscCode);

        EAIServices response = (EAIServices) webServiceClient.exchange(generateIfscSearchRequest(channelTraceId, ifscCode));
        validateOMWResponse(response);

        BankResultsDto bankResultsDto = new BankResultsDto(response.getBody().getAxisRemittanceIFSCDetailsRes());
        return bankResultsDto;

    }

    private void validateOMWResponse(EAIServices response) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {

            GenericExceptionHandler.handleError(TransferErrorCode.IFSC_CODE_NOT_FOUND,
                    response.getBody().getExceptionDetails().getErrorDescription(), response.getBody().getExceptionDetails().getErrorCode());
        }
    }

    private EAIServices generateIfscSearchRequest(String channelTraceId, String ifscCode) {
        EAIServices request = new EAIServices();
        request.setBody(new EAIServices.Body());
        request.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getIfscSearch(), channelTraceId));

        AxisRemittanceIFSCDetailsReqType ifscSearchRequest = new AxisRemittanceIFSCDetailsReqType();
        ifscSearchRequest.setCountry("IN");
        ifscSearchRequest.setIFSCCode(ifscCode);
        request.getBody().setAxisRemittanceIFSCDetailsReq(ifscSearchRequest);
        return request;
    }

}
