package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ThanigachalamP
 */
@RunWith(MockitoJUnitRunner.class)
public class CCBelongsToCifValidatorTest {

    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CCBelongsToCifValidator ccBelongsToCifValidator;

    private final String CC_NO = "E6BD9127E95D80C2C0D46DB2A314514C315A21C8408729F99ECA3D22D123DB2D";


    @Before
    public void before(){
        ReflectionTestUtils.setField(ccBelongsToCifValidator,"auditEventPublisher", auditEventPublisher);
        ReflectionTestUtils.setField(ccBelongsToCifValidator,"encryptionService", encryptionService);
    }

    private FundTransferRequestDTO buildFundTransferRequest(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setServiceType(ServiceType.LOCAL.getName());
        fundTransferRequestDTO.setToAccount("AE610240041520084750901");
        fundTransferRequestDTO.setCardNo("");
        return fundTransferRequestDTO;
    }

    private ValidationContext buildValidationContext(){
        ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details",buildCardDetails());
        validationContext.add("validate-from-account",true);
        validationContext.add("validate-to-account",true);
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

    private List<CardDetailsDTO> buildCardDetails(){
        List<CardDetailsDTO> cardDetailsDTOList = new ArrayList<>();
        CardDetailsDTO cardDetailsDTO = new CardDetailsDTO();
        cardDetailsDTO.setEncryptedCardNumber(CC_NO);
        cardDetailsDTO.setCardNo(CC_NO);
        cardDetailsDTOList.add(cardDetailsDTO);
        return cardDetailsDTOList;
    }

    @Test
    public void testCCBelongsToCIF(){

        FundTransferRequestDTO fundTransferRequestDTO = buildFundTransferRequest();
        fundTransferRequestDTO.setCardNo(CC_NO);
        RequestMetaData requestMetaData = buildRequestMetaData();
        Mockito.when(encryptionService.decrypt(CC_NO)).thenReturn(CC_NO);
        ValidationContext validationContext = buildValidationContext();

        ValidationResult validationResult = ccBelongsToCifValidator.validate(fundTransferRequestDTO, requestMetaData, validationContext);
        Assert.assertEquals(validationResult.isSuccess(), true);
    }

    @Test
    public void testCCNotBelongsToCIF(){

        FundTransferRequestDTO fundTransferRequestDTO = buildFundTransferRequest();
        RequestMetaData requestMetaData = buildRequestMetaData();
        Mockito.when(encryptionService.decrypt("")).thenReturn("");
        Mockito.when(encryptionService.decrypt(CC_NO)).thenReturn(CC_NO);
        ValidationContext validationContext = buildValidationContext();

        ValidationResult validationResult = ccBelongsToCifValidator.validate(fundTransferRequestDTO, requestMetaData, validationContext);
        Assert.assertEquals(validationResult.isSuccess(), false);
        Assert.assertEquals(validationResult.getTransferErrorCode(), TransferErrorCode.ACCOUNT_NOT_BELONG_TO_CIF);
    }
}
