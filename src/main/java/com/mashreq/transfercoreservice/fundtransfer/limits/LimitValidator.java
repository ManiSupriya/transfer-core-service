package com.mashreq.transfercoreservice.fundtransfer.limits;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.COOLING_LIMIT_AMOUNT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.COOLING_LIMIT_COUNT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DAILY_AMOUNT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DAILY_COUNT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_BEN_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.LIMIT_PACKAGE_NOT_DEFINED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.MIN_AMOUNT_LIMIT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.MONTHLY_AMOUNT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.MONTHLY_COUNT_REACHED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.TRX_AMOUNT_REACHED;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.LIMIT_VALIDATION;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.MIN_LIMIT_VALIDATION;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.getCodeByType;

import java.math.BigDecimal;
import java.util.Optional;

import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitCheckType;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.model.ServiceType;
import com.mashreq.transfercoreservice.repository.ServiceTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitValidator implements ILimitValidator{

    private final AsyncUserEventPublisher auditEventPublisher;
    private final ServiceTypeRepository serviceTypeRepository;
    private final LimitCheckService limitCheckService;
    private final MobCommonClient mobCommonClient;
    
    private String getRemarks(LimitValidatorResponse resultsDto, String cif, String paidAmount, String beneficiaryType) {
        return String.format(
                "Cif=%s,PaidAmount=%s,BeneType=%s,availableLimitAmount=%s,availableLimitCount=%s,maxAmountDaily=%s,maxAmountMonthly=%s,maxCountMonthly=%s,maxCountDaily=%s," +
                        "countRemark=%s,amountRemark=%s,transacationId=%s",
                cif,
                paidAmount,
                beneficiaryType,
                resultsDto.getCurrentAvailableAmount(),
                resultsDto.getCurrentAvailableCount(),
                resultsDto.getMaxAmountDaily(),
                resultsDto.getMaxAmountMonthly(),
                resultsDto.getMaxCountMonthly(),
                resultsDto.getMaxCountDaily(),
                resultsDto.getCountRemark(),
                resultsDto.getAmountRemark(),
                resultsDto.getTransactionRefNo()
        );

    }

    @Override
    public void validateMin(UserDTO userDTO, final String beneficiaryType, BigDecimal limitUsageAmount, RequestMetaData metadata) {
        log.info("[LimitValidator] Min limit validator called cif ={} and beneficiaryType={} and paidAmount={}",
                htmlEscape(userDTO.getCifId()), htmlEscape(beneficiaryType), htmlEscape(limitUsageAmount));
        ServiceType serviceType = getServiceType(beneficiaryType);
        BigDecimal minAmount = getMinAmount(serviceType.getMinAmount());
        String remarks = getRemarks(minAmount,userDTO.getCifId(),limitUsageAmount,beneficiaryType,LimitCheckType.MIN_AMOUNT.name());
        if(limitUsageAmount.compareTo(minAmount) < 0){
            auditEventPublisher.publishFailureEvent(MIN_LIMIT_VALIDATION, metadata, remarks,
                    MIN_AMOUNT_LIMIT_REACHED.getCustomErrorCode(), MIN_AMOUNT_LIMIT_REACHED.getErrorMessage(), null);
            GenericExceptionHandler.handleError(MIN_AMOUNT_LIMIT_REACHED,
                    MIN_AMOUNT_LIMIT_REACHED.getErrorMessage(), MIN_AMOUNT_LIMIT_REACHED.getErrorMessage());
        }
        auditEventPublisher.publishSuccessEvent(MIN_LIMIT_VALIDATION, metadata, remarks);
        log.info("Min Limit validation successful");
    }

    private BigDecimal getMinAmount(String minAmount) {
        if(minAmount == null){
            return BigDecimal.ZERO;
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

    @Override
    public LimitValidatorResponse validate(final UserDTO userDTO, final String beneficiaryType, final BigDecimal paidAmount, final RequestMetaData metaData,Long benId) {
        log.info("[LimitValidator] limit validator called cif ={} and beneficiaryType={} and paidAmount={} and countryId {} and segmentId {}",
                htmlEscape(userDTO.getCifId()), htmlEscape(beneficiaryType), htmlEscape(paidAmount), htmlEscape(metaData.getCountry()), htmlEscape(metaData.getSegment()));
        LimitValidatorResponse limitValidatorResultsDto = limitCheckService.validateLimit(userDTO.getCifId(), beneficiaryType, metaData.getCountry(),metaData.getSegment(),benId,paidAmount);
        log.info("limitValidatorResultsDto {}",limitValidatorResultsDto);
        String transactionRefNo = generateTransactionRefNo(limitValidatorResultsDto,metaData,beneficiaryType);
        limitValidatorResultsDto.setTransactionRefNo(transactionRefNo);

        final String remarks = getRemarks(limitValidatorResultsDto, metaData.getPrimaryCif(), String.valueOf(paidAmount), beneficiaryType);
        if (Boolean.FALSE.equals(limitValidatorResultsDto.getIsValid())) {
            if (LimitCheckType.MONTHLY_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, MONTHLY_AMOUNT_REACHED.getCustomErrorCode(), MONTHLY_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(MONTHLY_AMOUNT_REACHED,
                        MONTHLY_AMOUNT_REACHED.getErrorMessage());
            } else if (LimitCheckType.MONTHLY_COUNT.name().equals(limitValidatorResultsDto.getCountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, MONTHLY_COUNT_REACHED.getCustomErrorCode(), MONTHLY_COUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(MONTHLY_COUNT_REACHED,
                        MONTHLY_COUNT_REACHED.getErrorMessage());
            } else if (LimitCheckType.DAILY_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, DAILY_AMOUNT_REACHED.getCustomErrorCode(), DAILY_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(DAILY_AMOUNT_REACHED,
                        DAILY_AMOUNT_REACHED.getErrorMessage());
            } else if (LimitCheckType.DAILY_COUNT.name().equals(limitValidatorResultsDto.getCountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, DAILY_COUNT_REACHED.getCustomErrorCode(), DAILY_COUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(DAILY_COUNT_REACHED,
                        DAILY_COUNT_REACHED.getErrorMessage());
            }
            else if (LimitCheckType.COOLING_LIMIT_COUNT.name().equals(limitValidatorResultsDto.getCountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, COOLING_LIMIT_COUNT_REACHED.getCustomErrorCode(), COOLING_LIMIT_COUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(COOLING_LIMIT_COUNT_REACHED,
                        COOLING_LIMIT_COUNT_REACHED.getErrorMessage());
            }
            else if (LimitCheckType.COOLING_LIMIT_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, COOLING_LIMIT_AMOUNT_REACHED.getCustomErrorCode(), COOLING_LIMIT_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(COOLING_LIMIT_AMOUNT_REACHED,
                        COOLING_LIMIT_AMOUNT_REACHED.getErrorMessage());
            }
            else if (LimitCheckType.TRX_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, TRX_AMOUNT_REACHED.getCustomErrorCode(), TRX_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(TRX_AMOUNT_REACHED,
                        TRX_AMOUNT_REACHED.getErrorMessage());
            }
            else {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, TRX_AMOUNT_REACHED.getCustomErrorCode(), TRX_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(LIMIT_PACKAGE_NOT_DEFINED,
                        LIMIT_PACKAGE_NOT_DEFINED.getErrorMessage());
            }
        }
        auditEventPublisher.publishSuccessEvent(LIMIT_VALIDATION, metaData, remarks);
        log.info("Limit validation successful");
        return limitValidatorResultsDto;
    }

    public LimitValidatorResponse validateAvailableLimits(final UserDTO userDTO, final String beneficiaryType, final BigDecimal paidAmount, final RequestMetaData metaData,Long benId) {
        log.info("[LimitValidator] limit validator called cif ={} and beneficiaryType={} and paidAmount={} and countryId {} and segmentId {}",
                htmlEscape(userDTO.getCifId()), htmlEscape(beneficiaryType), htmlEscape(paidAmount), htmlEscape(metaData.getCountry()), htmlEscape(metaData.getSegment()));

        //Remove this below check
        //And call mobcommonclient to get the available limits
        //continue with the below error responses in case of verification type is ELIGIBLE. only if there are no remarks then continnue with other status
        LimitValidatorResponse limitValidatorResultsDto = limitCheckService.validateLimit(userDTO.getCifId(), beneficiaryType, metaData.getCountry(),metaData.getSegment(),benId,paidAmount);
        log.info("limitValidatorResultsDto {}",limitValidatorResultsDto);
        String transactionRefNo = generateTransactionRefNo(limitValidatorResultsDto,metaData,beneficiaryType);
        limitValidatorResultsDto.setTransactionRefNo(transactionRefNo);

        final String remarks = getRemarks(limitValidatorResultsDto, metaData.getPrimaryCif(), String.valueOf(paidAmount), beneficiaryType);
        if (Boolean.FALSE.equals(limitValidatorResultsDto.getIsValid())) {
            if (LimitCheckType.MONTHLY_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, MONTHLY_AMOUNT_REACHED.getCustomErrorCode(), MONTHLY_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(MONTHLY_AMOUNT_REACHED,
                        MONTHLY_AMOUNT_REACHED.getErrorMessage());
            } else if (LimitCheckType.MONTHLY_COUNT.name().equals(limitValidatorResultsDto.getCountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, MONTHLY_COUNT_REACHED.getCustomErrorCode(), MONTHLY_COUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(MONTHLY_COUNT_REACHED,
                        MONTHLY_COUNT_REACHED.getErrorMessage());
            } else if (LimitCheckType.DAILY_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, DAILY_AMOUNT_REACHED.getCustomErrorCode(), DAILY_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(DAILY_AMOUNT_REACHED,
                        DAILY_AMOUNT_REACHED.getErrorMessage());
            } else if (LimitCheckType.DAILY_COUNT.name().equals(limitValidatorResultsDto.getCountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, DAILY_COUNT_REACHED.getCustomErrorCode(), DAILY_COUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(DAILY_COUNT_REACHED,
                        DAILY_COUNT_REACHED.getErrorMessage());
            }
            else if (LimitCheckType.COOLING_LIMIT_COUNT.name().equals(limitValidatorResultsDto.getCountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, COOLING_LIMIT_COUNT_REACHED.getCustomErrorCode(), COOLING_LIMIT_COUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(COOLING_LIMIT_COUNT_REACHED,
                        COOLING_LIMIT_COUNT_REACHED.getErrorMessage());
            }
            else if (LimitCheckType.COOLING_LIMIT_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, COOLING_LIMIT_AMOUNT_REACHED.getCustomErrorCode(), COOLING_LIMIT_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(COOLING_LIMIT_AMOUNT_REACHED,
                        COOLING_LIMIT_AMOUNT_REACHED.getErrorMessage());
            }
            else if (LimitCheckType.TRX_AMOUNT.name().equals(limitValidatorResultsDto.getAmountRemark())) {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, TRX_AMOUNT_REACHED.getCustomErrorCode(), TRX_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(TRX_AMOUNT_REACHED,
                        TRX_AMOUNT_REACHED.getErrorMessage());
            }
            else {
                auditEventPublisher.publishFailureEvent(LIMIT_VALIDATION, metaData, remarks, TRX_AMOUNT_REACHED.getCustomErrorCode(), TRX_AMOUNT_REACHED.getErrorMessage(), "limit check failed");
                GenericExceptionHandler.handleError(LIMIT_PACKAGE_NOT_DEFINED,
                        LIMIT_PACKAGE_NOT_DEFINED.getErrorMessage());
            }
        }
        auditEventPublisher.publishSuccessEvent(LIMIT_VALIDATION, metaData, remarks);
        log.info("Limit validation successful");
        return limitValidatorResultsDto;
    }

    private String generateTransactionRefNo(LimitValidatorResponse validationResult, RequestMetaData metadata, String benCode) {
        return metadata.getChannel().substring(0,1) + getCodeByType(benCode) + validationResult.getTransactionRefNo();
    }

}