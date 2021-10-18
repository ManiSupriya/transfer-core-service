package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.model.*;
import com.mashreq.transfercoreservice.repository.QrStatusMsRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.HtmlUtils;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.InternationalFundTransferStrategy;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
@RunWith(MockitoJUnitRunner.class)
public class FundTransferServiceDefaultTest {
	
	
	@Mock
	RequestMetaData metaData; 
	@Mock
	FundTransferResponseDTO fundTransferResponseDTO;
	@Mock
	VerifyOTPRequestDTO verifyOTPRequestDTO;
	 @Mock
	 OTPService iamService;
	 @Mock
	 AsyncUserEventPublisher asyncUserEventPublisher;
	 @Mock
	 DigitalUser digitalUser;
	 @InjectMocks
	 FundTransferServiceDefault fundTransferServiceDefault;
	 @Mock
	 HtmlUtils htmlUtils;
	 @Mock
	 DigitalUserRepository digitalUserRepository;
	 @Mock
	 FundTransferStrategy strategy;
	 @Mock
	 FundTransferResponse fundTransferResponse;
	 @Mock
	 EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;
	 @Mock
	 InternationalFundTransferStrategy internationalFundTransferStrategy;
	 @Mock
	 QrStatusMsRepository qrStatusMsRepository;
	 @Mock
	 ExternalErrorCodeConfig errorCodeConfig;

	 FundTransferRequestDTO fundTransferRequestDTO;
	 
	 @Mock
	 CoreFundTransferResponseDto coreFundTransferResponseDto;
	 
	 @Before
	 public void prepare() {
		 QuickRemitStatusMaster quickRemitStatusMaster = new QuickRemitStatusMaster();
		 quickRemitStatusMaster.setStatusCode("EAI-FCI-BRK-001");
		 List<QuickRemitStatusMaster> quickRemitStatusMasters = Arrays.asList(quickRemitStatusMaster);

		 when(qrStatusMsRepository.findAll()).thenReturn(quickRemitStatusMasters);

	     this.fundTransferServiceDefault.init();
	     fundTransferRequestDTO = generateFundTransferRequest();
	     ReflectionTestUtils.setField(fundTransferServiceDefault, "activeProfile", "prod");
	 }
	
	@Test
	public void transferFundTest() {
		
		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(true);
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(any(), any(), any());
		when(iamService.verifyOTP(any())).thenReturn(Response.<VerifyOTPResponseDTO>builder().status(ResponseStatus.SUCCESS).data(verifyOTPResponseDTO).build());
		FundTransferResponseDTO fundTransferResponseDTO = fundTransferServiceDefault.transferFund(metaData,
				fundTransferRequestDTO);
		Assert.assertNull(fundTransferResponseDTO);
	}

	@Test
	public void handleFailureTest(){
		CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
		coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.F);
		FundTransferResponse fundTransferResponse = FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();

		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(true);

		when(errorCodeConfig.getMiddlewareExternalErrorCodesMap()).thenReturn(Collections.emptyMap());

		Assertions.assertThrows(GenericException.class,()-> fundTransferServiceDefault.handleFailure(fundTransferRequestDTO, fundTransferResponse));

		coreFundTransferResponseDto.setMwResponseCode("EAI-FCI-BRK-001");
		FundTransferResponse fundTransferResponse1 = FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();

		Assertions.assertThrows(GenericException.class,()-> fundTransferServiceDefault.handleFailure(fundTransferRequestDTO, fundTransferResponse1));
	}

	@Test
	public void transferFundTestOTPFailure() {
		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(false);
        Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(any(), any(), any(), any(), any(), any(), any());
		when(iamService.verifyOTP(any())).thenReturn(Response.<VerifyOTPResponseDTO>builder().status(ResponseStatus.FAIL).errorCode("TN-5016").errorDetails("Something went wrong with OTP external service").data(verifyOTPResponseDTO).build());
		try {
			fundTransferServiceDefault.transferFund(metaData,
					fundTransferRequestDTO);
		} catch (GenericException genericException) {
			assertEquals("TN-5016", genericException.getErrorCode());
		}
	}
	
	@Test
	public void getFundTransferResponseInvalidCIFTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		metaData.setPrimaryCif("123456");
        Class<?>[] paramTypes = new Class<?>[2];
		paramTypes[0] = RequestMetaData.class;
		paramTypes[1] = FundTransferRequestDTO.class;
		when(metaData.getPrimaryCif()).thenReturn("111");
		Method method = fundTransferServiceDefault.getClass().getDeclaredMethod("getFundTransferResponse", paramTypes);
		method.setAccessible(true);
		try {
		method.invoke(fundTransferServiceDefault, metaData, fundTransferRequestDTO);
		}
		catch(InvocationTargetException ie) {
			assertTrue(ie.getCause().toString().contains("TN-1002"));
			
		}
	}
	@Test
	public void getFundTransferResponsevalidCIFTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?>[] paramTypes = new Class<?>[2];
		paramTypes[0] = RequestMetaData.class;
		paramTypes[1] = FundTransferRequestDTO.class;
		when(metaData.getPrimaryCif()).thenReturn("111");
		Segment segment = new Segment();
		segment.setId(1L);
		 Country country = new Country();
		 country.setId(1L);
		 country.setLocalCurrency("AED");
		DigitalUserGroup digitalUserGroup = new DigitalUserGroup();
		digitalUserGroup.setSegment(segment);
		digitalUserGroup.setCountry(country);
		DigitalUser digitalUser = new DigitalUser();
		digitalUser.setId(2L);
		digitalUser.setCif("1234");
		coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
		digitalUser.setDigitalUserGroup(digitalUserGroup);
		Optional<DigitalUser> userOptional = Optional.of(digitalUser);
		when(digitalUserRepository.findByCifEquals(Mockito.anyString())).thenReturn(userOptional);
		fundTransferResponse = FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();
		strategy = new InternationalFundTransferStrategy(null, null, null, null, null, null, null, null, null, null, null, null, null,null);
		Method method = fundTransferServiceDefault.getClass().getDeclaredMethod("getFundTransferResponse", paramTypes);
		method.setAccessible(true);
		
		try {
			method.invoke(fundTransferServiceDefault, metaData, fundTransferRequestDTO);
			}
			catch(InvocationTargetException ie) {
				assertFalse(ie.getCause().toString().contains("TN-1002"));
				
			}
	}
	
	private FundTransferRequestDTO generateFundTransferRequest() {
		fundTransferRequestDTO = new FundTransferRequestDTO();
		fundTransferRequestDTO.setFinalBene("cad internauser");
		fundTransferRequestDTO.setPurposeCode("PIN");
		fundTransferRequestDTO.setPurposeDesc("Personal Investments");
		fundTransferRequestDTO.setOtp("12345");
		fundTransferRequestDTO.setAdditionalField("int aed to cad");
		fundTransferRequestDTO.setBeneficiaryId("236");
		fundTransferRequestDTO.setAmount(new BigDecimal(100));
		fundTransferRequestDTO.setCurrency("AED");
		fundTransferRequestDTO.setFinTxnNo("FTO-MAE-010314310-200930145737");
		fundTransferRequestDTO.setFromAccount("011248071719");
		fundTransferRequestDTO.setServiceType("INFT");
		fundTransferRequestDTO.setToAccount("010893120906");
		fundTransferRequestDTO.setTxnCurrency("CAD");
		fundTransferRequestDTO.setChallengeToken("test");
		fundTransferRequestDTO.setChargeBearer("B");
		fundTransferRequestDTO.setDpRandomNumber("EF4EEE95A2022C00344195AD3FAF4206");
		fundTransferRequestDTO.setDpPublicKeyIndex(12);
		return fundTransferRequestDTO;
	}

}
