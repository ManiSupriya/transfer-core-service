package com.mashreq.transfercoreservice.fundtransfer.limits;



import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Map;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ERROR_LIMIT_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitCheckService {

    private final DataSource dataSource;

    public LimitValidatorResponse validateLimit(String cifId, String beneficiaryTypeCode, String country,String segment,Long beneId,BigDecimal amount) {
        try {
            JdbcTemplate template = new JdbcTemplate(dataSource);
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(template).withProcedureName("USP_VALIDATE_LIMIT");
            MapSqlParameterSource input = new MapSqlParameterSource();
            input.addValue("cifId", cifId);
            input.addValue("trxType", beneficiaryTypeCode);
            input.addValue("countryCode", country);
            input.addValue("segment", segment);
            input.addValue("beneId",  beneId);
            input.addValue("payAmountInLocalCurrency", amount);

            Map<String, Object> output = simpleJdbcCall.execute(input);


            return LimitValidatorResponse.builder()
                    .errorCode(String.valueOf(output.get("errorCode")))
                    .isValid((Boolean) output.get("isValid"))
                    .countRemark(String.valueOf(output.get("countRemark")))
                    .amountRemark(String.valueOf(output.get("amountRemark")))
                    .currentAvailableCount((BigDecimal) output.get("currentAvailableCount"))
                    .currentAvailableAmount((BigDecimal) output.get("currentAvailableAmount"))
                    .monthlyUsedCount(String.valueOf(output.get("monthlyUsedCount")))
                    .maxCountMonthly(String.valueOf( output.get("maxCountMonthly")))
                    .dailyUsedCount(String.valueOf( output.get("dailyUsedCount")))
                    .maxCountDaily(String.valueOf(output.get("maxCountDaily")))
                    .maxTrxAmount(String.valueOf(output.get("maxTrxAmout")))
                    .maxAmountDaily(String.valueOf(output.get("maxAmountDaily")))
                    .dailyUsedAmount(String.valueOf(output.get("dailyUsedAmount")))
                    .maxAmountMonthly(String.valueOf( output.get("maxAmountMonthly")))
                    .monthlyUsedAmount(String.valueOf(output.get("monthlyUsedAmount")))
                    .coolingLimitCount(String.valueOf(output.get("coolingLimitCount")))
                    .coolingLimitAmount(String.valueOf(output.get("coolingLimitAmount")))
                    .transactionRefNo(String.valueOf(output.get("transactionRefNo")))
                    .limitVersionUuid(String.valueOf(output.get("limitVersionUuid")))
                    .build();
        } catch (Exception e) {
            log.error("Error for limit validator={}", e.getMessage());
            GenericExceptionHandler.handleError(ERROR_LIMIT_CHECK, ERROR_LIMIT_CHECK.getErrorMessage());
        }
        return null;
    }
}
