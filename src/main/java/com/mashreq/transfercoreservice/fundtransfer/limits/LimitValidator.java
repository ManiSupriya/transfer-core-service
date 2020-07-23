package com.mashreq.transfercoreservice.fundtransfer.limits;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitCheckType;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.model.ServiceType;
import com.mashreq.transfercoreservice.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.LIMIT_VALIDATION;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.MIN_LIMIT_VALIDATION;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitValidator {

    private final MobCommonService mobCommonService;
    private final AsyncUserEventPublisher auditEventPublisher;
    private final ServiceTypeRepository serviceTypeRepository;

    /**
     * Method to get the limits and validate against user's consumed limit
     */
    public LimitValidatorResultsDto validate(final UserDTO userDTO, final String beneficiaryType, final BigDecimal paidAmount, final RequestMetaData metaData) {
        log.info("[LimitValidator] limit validator called cif ={} and beneficiaryType={} and paidAmount={}",
                htmlEscape(userDTO.getCifId()), htmlEscape(beneficiaryType), htmlEscape(paidAmount.toString()));
        LimitValidatorResultsDto limitValidatorResultsDto =
                mobCommonService.validateAvailableLimit(userDTO.getCifId(), beneficiaryType, paidAmount);

        final String remarks = getRemarks(limitValidatorResultsDto, metaData.getPrimaryCif(), String.valueOf(paidAmount), beneficiaryType);
        if (!limitValidatorResultsDto.isValid()) {

            if (LimitCheckType.DAILY_AMOUNT.equals(limitValidatorResultsDto.getLimitCheckType())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks,
                        DAY_AMOUNT_LIMIT_REACHED.getCustomErrorCode(), DAY_AMOUNT_LIMIT_REACHED.getErrorMessage(), null);
                GenericExceptionHandler.handleError(DAY_AMOUNT_LIMIT_REACHED,
                        DAY_AMOUNT_LIMIT_REACHED.getErrorMessage());
            } else {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks,
                        MONTH_AMOUNT_LIMIT_REACHED.getCustomErrorCode(), MONTH_AMOUNT_LIMIT_REACHED.getErrorMessage(), null);
                GenericExceptionHandler.handleError(MONTH_AMOUNT_LIMIT_REACHED,
                        MONTH_AMOUNT_LIMIT_REACHED.getErrorMessage());
            }
        }
        auditEventPublisher.publishSuccessEvent(LIMIT_VALIDATION, metaData, remarks);
        log.info("Limit validation successful");
        return limitValidatorResultsDto;
    }

    private String getRemarks(LimitValidatorResultsDto resultsDto, String cif, String paidAmount, String beneficiaryType) {
        return String.format(
                "Cif=%s,PaidAmount=%s,BeneType=%s,availableLimitAmount=%s,maxAmountDaily=%s,maxAmountMonthly=%s,limitCheckType=%s",
                cif,
                paidAmount,
                beneficiaryType,
                resultsDto.getAvailableLimitAmount(),
                resultsDto.getMaxAmountDaily(),
                resultsDto.getMaxAmountMonthly(),
                resultsDto.getLimitCheckType()
        );

    }

    public void validateMin(UserDTO userDTO, final String beneficiaryType, BigDecimal limitUsageAmount, RequestMetaData metadata) {
        log.info("[LimitValidator] Min limit validator called cif ={} and beneficiaryType={} and paidAmount={}",
                htmlEscape(userDTO.getCifId()), htmlEscape(beneficiaryType), htmlEscape(limitUsageAmount.toString()));
        ServiceType serviceType = getServiceType(beneficiaryType);
        BigDecimal minAmount = getMinAmount(serviceType.getMinAmount());
        String remarks = getRemarks(minAmount,userDTO.getCifId(),limitUsageAmount,beneficiaryType,LimitCheckType.MIN_AMOUNT.name());
        if(minAmount.compareTo(limitUsageAmount) <0){
            auditEventPublisher.publishFailureEvent(MIN_LIMIT_VALIDATION, metadata, remarks,
                    MIN_AMOUNT_LIMIT_REACHED.getCustomErrorCode(), MIN_AMOUNT_LIMIT_REACHED.getErrorMessage(), null);
            GenericExceptionHandler.handleError(MIN_AMOUNT_LIMIT_REACHED,
                    MIN_AMOUNT_LIMIT_REACHED.getErrorMessage());
        }
        auditEventPublisher.publishSuccessEvent(MIN_LIMIT_VALIDATION, metadata, remarks);
        log.info("Min Limit validation successful");
    }

    private BigDecimal getMinAmount(String minAmount) {
        if(minAmount == null){
            return new BigDecimal(0);
        }
        else return new BigDecimal(minAmount);
    }

    private ServiceType getServiceType(String benCode) {
        Optional<ServiceType> serviceTypeOptional = serviceTypeRepository.findByCodeEquals(benCode);
        if (!serviceTypeOptional.isPresent()) {
            GenericExceptionHandler.handleError(INVALID_BEN_CODE, INVALID_BEN_CODE.getErrorMessage());
        }
        log.info("Digital User found successfully {} ", serviceTypeOptional.get());

        return serviceTypeOptional.get();
    }

    private String getRemarks(BigDecimal minAmount, String cif, BigDecimal paidAmount, String beneficiaryType, String limitCheckType) {
        return String.format(
                "Cif=%s,PaidAmount=%s,BeneType=%s,minAmount=%s,limitCheckType=%s",
                cif,
                paidAmount,
                beneficiaryType,
                minAmount,
                limitCheckType
        );

    }

}