package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.base.Enums;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.AdditionalFields;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shahbazkh
 * @date 4/1/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryService {

    public static final String TRUE = "true";
    private final BeneficiaryClient beneficiaryClient;
    private final AsyncUserEventPublisher userEventPublisher;

    public BeneficiaryDto getByIdWithoutValidation(final String cifId, Long id, String journeyVersion, RequestMetaData metaData) {
        log.info("Fetching Beneficiary for id = {}", id);
        Response<BeneficiaryDto> response = "V2".equals(journeyVersion) ?
                beneficiaryClient.getByIdWithoutValidationV2(cifId, id):
                beneficiaryClient.getByIdWithoutValidation(cifId, id);

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            userEventPublisher.publishFailureEvent(FundTransferEventType.FUNDTRANSFER_BENDETAILS, metaData, "failed to get ben details", response.getErrorCode(), response.getMessage(), response.getMessage());
            GenericExceptionHandler.handleError(BENE_EXTERNAL_SERVICE_ERROR, BENE_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    getErrorDetails(response));
        }
        log.info("Beneficiary fetched successfully for id = {}", id);
        return response.getData();
    }
    
    public BeneficiaryDto getById(Long id, String journeyVersion, RequestMetaData metaData, String validationType) {
        log.info("Fetching Beneficiary for id = {}", id);
        Response<BeneficiaryDto> response = "V2".equals(journeyVersion) ?
                beneficiaryClient.getByIdV2(metaData.getPrimaryCif(), validationType, id) :
                beneficiaryClient.getById(metaData.getPrimaryCif(), validationType, id);

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            userEventPublisher.publishFailureEvent(FundTransferEventType.FUNDTRANSFER_BENDETAILS, metaData, "failed to get ben details", response.getErrorCode(), response.getMessage(), response.getMessage());
            GenericExceptionHandler.handleError(BENE_EXTERNAL_SERVICE_ERROR, BENE_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    getErrorDetails(response));
        }
        log.info("Beneficiary fetched successfully for id = {}", id);
        return response.getData();
    }

    public BeneficiaryDto getUpdate(AdditionalFields additionalFields, Long id, String journeyVersion, RequestMetaData metaData, String validationType) {
        log.info("Fetching Beneficiary Update for id = {}", id);

        if("V2".equals(journeyVersion)){
            additionalFields.setNewVersion(true);
        }

        Response<BeneficiaryDto> response = beneficiaryClient.update(additionalFields, id, validationType);

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            GenericExceptionHandler.handleError(BENE_EXTERNAL_SERVICE_ERROR, BENE_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    getErrorDetails(response));
        }
        log.info("Beneficiary fetched successfully for id = {}", id);

        return response.getData();
    }

    public static String getErrorDetails(Response response) {
        if (StringUtils.isNotBlank(response.getErrorDetails())) {
            return response.getErrorCode() + "," + response.getErrorDetails() + "," + response.getMessage();
        }
        return response.getErrorCode() + "," + response.getMessage();
    }

}
