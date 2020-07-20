package com.mashreq.transfercoreservice.fundtransfer.limits;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitCheckType;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DAY_AMOUNT_LIMIT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.MONTH_AMOUNT_LIMIT_REACHED;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.LIMIT_VALIDATION;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitValidator {

    private final MobCommonService mobCommonService;
    private final AsyncUserEventPublisher auditEventPublisher;

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
}