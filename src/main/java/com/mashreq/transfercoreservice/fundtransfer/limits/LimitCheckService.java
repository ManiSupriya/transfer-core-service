package com.mashreq.transfercoreservice.fundtransfer.limits;



import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ERROR_LIMIT_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitCheckService {

    private final DataSource dataSource;

    private static <T> T convertInstanceOfObject(Object obj, Class<T> clazz){
        if(obj != null)
            return clazz.cast(obj);
        return null;
    }

    public LimitValidatorResponse validateLimit(String cifId, String beneficiaryTypeCode, String country,String segment,Long beneId,BigDecimal amount) {
        LimitValidatorResponse limitValidatorResponse = null;
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

            Map<String, Object> storedProcOutput = simpleJdbcCall.execute(input);

            limitValidatorResponse = Optional.ofNullable(storedProcOutput.get("#result-set-1"))
                    .map(x -> (List) x)
                    .filter(CollectionUtils::isNotEmpty)
                    .map(x -> x.get(0))
                    .map(x -> (Map<String,Object>) x)
                    .map(this::mapResultSet)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error for limit validator={}", e.getMessage());
            GenericExceptionHandler.handleError(ERROR_LIMIT_CHECK, ERROR_LIMIT_CHECK.getErrorMessage());
        }
        return limitValidatorResponse;
    }

    private LimitValidatorResponse mapResultSet(Map<String, Object> storedProcOutput) {
        return LimitValidatorResponse.builder()
                .errorCode(convertInstanceOfObject(storedProcOutput.get("errorCode"), String.class))
                .isValid(convertInstanceOfObject(storedProcOutput.get("isValid"), Boolean.class))
                .countRemark(convertInstanceOfObject(storedProcOutput.get("countRemark"), String.class))
                .amountRemark(convertInstanceOfObject(storedProcOutput.get("amountRemark"), String.class))
                .transactionRefNo(convertInstanceOfObject(storedProcOutput.get("transactionRefNo"), String.class))
                .limitVersionUuid(convertInstanceOfObject(storedProcOutput.get("limitVersionUuid"), String.class))
                .currentAvailableCount(convertInstanceOfObject(storedProcOutput.get("currentAvailableCount"), Integer.class))
                .currentAvailableAmount(convertInstanceOfObject(storedProcOutput.get("currentAvailableAmount"), BigDecimal.class))
                .build();
    }
}
