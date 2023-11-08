package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author ThanigachalamP
 */
@ExtendWith(MockitoExtension.class)
public class CCBalanceValidatorTest {

    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @InjectMocks
    private CCBalanceValidator ccBalanceValidator;

    @BeforeEach
    public void before(){
        ReflectionTestUtils.setField(ccBalanceValidator,"auditEventPublisher", auditEventPublisher);
    }

    private ValidationContext buildValidationContext(){
        ValidationContext validationContext = new ValidationContext();
        validationContext.add("from-account",buildCardDetails());
        validationContext.add("transfer-amount-in-source-currency",new BigDecimal("90.10"));
        return validationContext;
    }

    private RequestMetaData buildRequestMetaData(){
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setPrimaryCif("012441750");
        requestMetaData.setChannel("Web");
        requestMetaData.setUsername("TEST_CUSTOMER");
        requestMetaData.setCountry("AE");
        requestMetaData.setEmail("thanigachalamp@mashreq.com");
        requestMetaData.setSegment("NEO");
        return requestMetaData;
    }

    private CardDetailsDTO buildCardDetails(){
        CardDetailsDTO cardDetailsDTO = new CardDetailsDTO();
        cardDetailsDTO.setAvailableCreditLimit(new BigDecimal("100.10"));
        return cardDetailsDTO;
    }

    private FundTransferRequestDTO buildFundTransferRequest(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setServiceType(ServiceType.LOCAL.getName());
        return fundTransferRequestDTO;
    }

    @Test
    public void testCCWithAvailableBalance(){

        FundTransferRequestDTO fundTransferRequestDTO = buildFundTransferRequest();
        RequestMetaData requestMetaData = buildRequestMetaData();
        ValidationContext validationContext = buildValidationContext();
        ValidationResult validationResult = ccBalanceValidator.validate(fundTransferRequestDTO, requestMetaData, validationContext);
        assertEquals(validationResult.isSuccess(), true);
    }

    @Test
    public void testCCWithNoAvailableBalance(){
        FundTransferRequestDTO fundTransferRequestDTO = buildFundTransferRequest();
        RequestMetaData requestMetaData = buildRequestMetaData();
        ValidationContext validationContext = buildValidationContext();
        validationContext.add("transfer-amount-in-source-currency",new BigDecimal("180.10"));
        ValidationResult validationResult = ccBalanceValidator.validate(fundTransferRequestDTO, requestMetaData, validationContext);
        assertEquals(validationResult.isSuccess(), false);
        assertEquals(validationResult.getTransferErrorCode(), TransferErrorCode.FT_CC_BALANCE_NOT_SUFFICIENT);
    }
}
