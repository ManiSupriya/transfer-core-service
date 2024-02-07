package com.mashreq.transfercoreservice.fundtransfer.limits;


import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.repository.ServiceTypeRepository;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.mashreq.transfercoreservice.client.mobcommon.dto.LimitCheckType.*;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class LimitValidatorTest {

    @Mock
    AsyncUserEventPublisher auditEventPublisher;
    @Mock
    ServiceTypeRepository serviceTypeRepository;

    @Mock
    LimitCheckService limitCheckService;

    @Mock
    MobCommonClient mobCommonClient;

    @InjectMocks
    LimitValidator limitValidator;

    @Test
    public void testValidateAvailableLimitsErrorStatus() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .build();

        Long benId = 654321L;

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.ERROR);
        response.setData(new LimitValidatorResponse());

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        GenericException exception = Assertions.assertThrows(GenericException.class, ()->{
            limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);
        });

        assertEquals(EXTERNAL_SERVICE_ERROR.getCustomErrorCode(), exception.getErrorCode());
    }

    @Test
    public void testValidateAvailableLimitsErrorNoResponseData() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .build();

        Long benId = 654321L;

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        GenericException exception = Assertions.assertThrows(GenericException.class, ()->{
            limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);
        });

        assertEquals(EXTERNAL_SERVICE_ERROR.getCustomErrorCode(), exception.getErrorCode());
    }

    @Test
    public void testValidateAvailableLimitsErrorDailyCountRemark() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;

        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .countRemark(DAILY_COUNT.name())
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        GenericException exception = Assertions.assertThrows(GenericException.class, ()->{
            limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);
        });

        assertEquals(DAILY_COUNT_REACHED.getCustomErrorCode(), exception.getErrorCode());
    }

    @Test
    public void testValidateAvailableLimitsErrorDailyAmountRemark() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;

        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .amountRemark(MONTHLY_AMOUNT.name())
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        GenericException exception = Assertions.assertThrows(GenericException.class, ()->{
            limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);
        });

        assertEquals(MONTHLY_AMOUNT_REACHED.getCustomErrorCode(), exception.getErrorCode());
    }

    @Test
    public void testValidateAvailableLimitsErrorDailyAmountRemarkAndNextLimitChangeDateIsNotNull() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;

        String nextLimitChangeDate = LocalDate.now().plusDays(1).toString();

        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .amountRemark(DAILY_AMOUNT.name())
                .nextLimitChangeDate(nextLimitChangeDate)
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        GenericException exception = Assertions.assertThrows(GenericException.class, ()->{
            limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);
        });

        assertEquals(DAILY_AMOUNT_REACHED.getCustomErrorCode(), exception.getErrorCode());
    }

    @Test
    public void testValidateAvailableLimitsLimitChangeCountsAreSame() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;

        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .amountRemark(DAILY_AMOUNT.name())
                .maxMonthlyLimitChangeCount(2)
                .usedMonthlyLimitChangeCount(2)
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        LimitValidatorResponse  limitValidatorResultsDto =  limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);

        assertEquals(FundsTransferEligibility.LIMIT_INCREASE_NOT_ELIGIBLE.name(), limitValidatorResultsDto.getVerificationType());
    }

    @Test
    public void testValidateAvailableLimitsNexLimitChangeDateIsNotNull() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;

        String nextLimitChangeDate = LocalDate.now().plusDays(1).toString();

        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .nextLimitChangeDate(nextLimitChangeDate)
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        LimitValidatorResponse  limitValidatorResultsDto =  limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);

        assertEquals(FundsTransferEligibility.LIMIT_INCREASE_NOT_ELIGIBLE.name(), limitValidatorResultsDto.getVerificationType());
    }

    @Test
    public void testValidateAvailableLimitsDailyAmountExceeded() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;

        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .amountRemark(DAILY_AMOUNT.name())
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        LimitValidatorResponse  limitValidatorResultsDto =  limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);

        assertEquals(DAILY_AMOUNT_REACHED.getCustomErrorCode(), limitValidatorResultsDto.getErrorCode());
        assertEquals(FundsTransferEligibility.LIMIT_INCREASE_ELIGIBLE.name(), limitValidatorResultsDto.getVerificationType());
    }

    @Test
    public void testValidateAvailableLimitsTransactionAmountExceeded() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;

        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .amountRemark(TRX_AMOUNT.name())
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        LimitValidatorResponse  limitValidatorResultsDto =  limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);

        assertEquals(TRX_AMOUNT_REACHED.getCustomErrorCode(), limitValidatorResultsDto.getErrorCode());
        assertEquals(FundsTransferEligibility.LIMIT_INCREASE_ELIGIBLE.name(), limitValidatorResultsDto.getVerificationType());
    }

    @Test
    public void testValidateAvailableLimitsErrorWrongRemarkCode() {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId("123456");

        String beneficiaryType = "INFT";
        BigDecimal paidAmount = new BigDecimal(1000);

        RequestMetaData metaData = RequestMetaData.builder()
                .primaryCif("123456")
                .channel("MOBILE")
                .build();

        Long benId = 654321L;


        LimitValidatorResponse limitValidatorResponse = LimitValidatorResponse.builder()
                .isValid(false)
                .amountRemark("RANDOM")
                .build();

        Response<LimitValidatorResponse> response = new Response<>();
        response.setStatus(ResponseStatus.SUCCESS);
        response.setData(limitValidatorResponse);

        Mockito.when(mobCommonClient.getAvailableLimits(any())).thenReturn(response);

        GenericException exception = Assertions.assertThrows(GenericException.class, ()->{
            limitValidator.validateAvailableLimits(userDTO, beneficiaryType, paidAmount, metaData, benId);
        });

        assertEquals(LIMIT_PACKAGE_NOT_DEFINED.getCustomErrorCode(), exception.getErrorCode());
    }
}
