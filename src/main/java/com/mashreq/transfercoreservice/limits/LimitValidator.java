package com.mashreq.transfercoreservice.limits;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserLimitUsageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Note: we are considering only segment level limit
 * we may need to add/handle CIf and user level limit
 * Below code has some provision and need to refactor once requirement will come
 *
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitValidator {

    private final LimitService limitService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;

    /**
     * Method to get the limits and validate against user's consumed limit
     */
    public LimitValidatorResultsDto validate(final UserDTO userDTO, final String beneficiaryType, final BigDecimal paidAmount) throws GenericException {

//        return LimitValidatorResultsDto.builder()
//                .availableLimitAmount(new BigDecimal("5000"))
//                .availableLimitCount(100)
//                .limitVersionUuid("TEST")
//                .isValid(true)
//                .build();

        log.info("[LimitValidator] - Validating limit for cif={} and billerType={} ", userDTO.getCifId(), beneficiaryType);
        long startTime = System.nanoTime();

        Optional<LimitDTO> defaultLP = limitService.getDefaultLPByBeneficiaryTypeAndSegmentAndCountry(
                beneficiaryType,
                userDTO.getSegmentId(),
                userDTO.getCountryId()
        );

        if(!defaultLP.isPresent()){
            log.warn("[LimitValidator] - Limit is not defined for biller type {} ", beneficiaryType);
            GenericExceptionHandler.handleError(TransferErrorCode.LIMIT_PACKAGE_NOT_FOUND, "");
        }

        LimitValidatorResultsDto limitValidatorResultsDto = validateLimitWithDefaultLP(defaultLP.get(), beneficiaryType, paidAmount, userDTO);

        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        log.info("[LimitValidator] - Limit validation is successful in {} ", totalTime);

        return limitValidatorResultsDto;

    }

    /**
     * Service to check with default segment and country level limit package
     *
     * If there is no usage monthly then defaultLP is available
     * If there is monthly usage but no daily usage then Default - Monthly is available
     * If there is monthly, daily usage then MIN OF (default - monthly) or (default - daily)
     */
    private LimitValidatorResultsDto validateLimitWithDefaultLP(LimitDTO limitDTO, String beneficiaryType,
                                                                BigDecimal paidAmount, UserDTO userDTO) {

        LimitValidatorResultsDto limitValidatorResultsDto = LimitValidatorResultsDto.builder().build();
        limitValidatorResultsDto.setLimitVersionUuid(limitDTO.getVersionUuid());

        validateTransactionLimit(paidAmount, limitDTO.getMaxTrxAmount());

        /*
         * Validate monthly usage
         */
        log.info("[LimitValidator] - Get monthly limit usage for beneficiaryType={} and cif={}", beneficiaryType, userDTO.getCifId());
        Optional<UserLimitUsageDTO> userMonthlyLimitUsageDTO = digitalUserLimitUsageService.getMonthlyLimitUsageByBillerTypeAndCif(
                beneficiaryType, userDTO.getCifId());
        if (!userMonthlyLimitUsageDTO.isPresent()){
            limitValidatorResultsDto.setAvailableLimitAmount(limitDTO.getMaxAmountMonthly().min(limitDTO.getMaxTrxAmount()));
            limitValidatorResultsDto.setAvailableLimitCount(limitDTO.getMaxCountDaily());
            return limitValidatorResultsDto;
        }
        Integer monthlyAvailableCount = validateMonthlyCount(userMonthlyLimitUsageDTO.get().getUsedCount(), limitDTO.getMaxCountMonthly());
        BigDecimal monthlyAvailableAmount = validateMonthlyAmount(userMonthlyLimitUsageDTO.get().getUsedAmount(), limitDTO.getMaxAmountMonthly(),
                paidAmount);

        /*
         * Validate daily usage
         */
        log.info("[LimitValidator] - Get monthly limit usage for beneficiaryType={} and cif={}", beneficiaryType, userDTO.getCifId());
        Optional<UserLimitUsageDTO> userDailyLimitUsageDTO = digitalUserLimitUsageService.getDailyLimitUsageByBillerTypeAndCif(
                beneficiaryType, userDTO.getCifId());
        if(!userDailyLimitUsageDTO.isPresent()){
            limitValidatorResultsDto.setAvailableLimitAmount(monthlyAvailableAmount.min(limitDTO.getMaxTrxAmount()));
            limitValidatorResultsDto.setAvailableLimitCount(monthlyAvailableCount);
            return limitValidatorResultsDto;
        }
        Integer dailyAvailableCount = validateDailyCount(userDailyLimitUsageDTO.get().getUsedCount(), limitDTO.getMaxCountDaily());
        BigDecimal dailyAvailableAmount = validateDailyAmount(userDailyLimitUsageDTO.get().getUsedAmount(), limitDTO.getMaxAmountDaily(),
                paidAmount);

        /**
         * vailableLimitAmount = MIN of daily amount, monthly amount
         * AvailableLimitCount = MIN of daily count and month count
         */
        limitValidatorResultsDto.setAvailableLimitAmount(
                dailyAvailableAmount.min(monthlyAvailableAmount).min(limitDTO.getMaxTrxAmount()));

        limitValidatorResultsDto.setAvailableLimitCount(
                Math.min(monthlyAvailableCount, dailyAvailableCount));

        return limitValidatorResultsDto;
    }

    private Integer validateMonthlyCount(Integer usedCount, Integer countAllowed) {
        Integer availableCount = countAllowed - usedCount;
        if(availableCount - 1 <  0){
            GenericExceptionHandler.handleError(TransferErrorCode.MONTH_COUNT_LIMIT_REACHED, "");
        }
        return availableCount;
    }

    private Integer validateDailyCount(Integer usedCount, Integer countAllowed) {
        Integer availableCount = countAllowed - usedCount;
        if(availableCount - 1 <  0){
            GenericExceptionHandler.handleError(TransferErrorCode.DAY_COUNT_LIMIT_REACHED, "");
        }
        return availableCount;
    }

    private BigDecimal validateMonthlyAmount(final BigDecimal usedAmount, BigDecimal amountAllowed, BigDecimal paidAmount) {
        BigDecimal availableAmount = amountAllowed.subtract(usedAmount);

        if(availableAmount.subtract(paidAmount).compareTo(new BigDecimal(0)) == -1){
            GenericExceptionHandler.handleError(TransferErrorCode.MONTH_AMOUNT_LIMIT_REACHED, "");
        }
        return availableAmount;
    }

    private BigDecimal validateDailyAmount(final BigDecimal usedAmount, BigDecimal amountAllowed, BigDecimal paidAmount) {
        BigDecimal availableAmount = amountAllowed.subtract(usedAmount);

        if(availableAmount.subtract(paidAmount).compareTo(new BigDecimal(0)) == -1){
            GenericExceptionHandler.handleError(TransferErrorCode.DAY_AMOUNT_LIMIT_REACHED, "");
        }
        return availableAmount;
    }

    private void validateTransactionLimit(final BigDecimal paidAmount, final BigDecimal maxTrxAmount) {
        if(maxTrxAmount.compareTo(paidAmount) == -1){
            GenericExceptionHandler.handleError(TransferErrorCode.TRX_LIMIT_REACHED, "");
        }
    }
}