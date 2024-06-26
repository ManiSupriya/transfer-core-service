package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitResponseDto;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.HtmlUtils;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.InternationalFundTransferStrategy;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.model.DigitalUserGroup;
import com.mashreq.transfercoreservice.model.QuickRemitStatusMaster;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.transfercoreservice.repository.QrStatusMsRepository;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;

@ExtendWith(MockitoExtension.class)
public class FundTransferServiceDefaultTest {
	@Mock
	RequestMetaData metaData; 
	@Mock
	FundTransferResponseDTO fundTransferResponseDTO;
	@Mock
	VerifyOTPRequestDTO verifyOTPRequestDTO;
	 @Mock
	 AsyncUserEventPublisher asyncUserEventPublisher;
	 @Mock
	 private DigitalUserLimitUsageService digitalUserLimitUsageService;
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
	 @Mock
	 private TwoFactorAuthRequiredCheckService service;
	 FundTransferRequestDTO fundTransferRequestDTO;
	 
	 @Mock
	 CoreFundTransferResponseDto coreFundTransferResponseDto;

	@Mock
	TransferLimitService transferLimitService;

	@BeforeEach
	 public void prepare() {
		 QuickRemitStatusMaster quickRemitStatusMaster = new QuickRemitStatusMaster();
		 quickRemitStatusMaster.setStatusCode("EAI-FCI-BRK-001");
		 List<QuickRemitStatusMaster> quickRemitStatusMasters = Arrays.asList(quickRemitStatusMaster);

	     this.fundTransferServiceDefault.init();
	     fundTransferRequestDTO = generateFundTransferRequest();
	     ReflectionTestUtils.setField(fundTransferServiceDefault, "activeProfile", "prod");
	     ReflectionTestUtils.setField(fundTransferServiceDefault, "cprEnabled", false);
	 }
	
	@Test
	public void transferFundTest() {
		
		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(true);
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
				new TwoFactorAuthRequiredCheckResponseDto();
		twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
		//when
		when(service.checkIfTwoFactorAuthenticationRequired(any(),
				any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(any(), any(), any());

		FundTransferResponseDTO fundTransferResponseDTO = fundTransferServiceDefault.transferFund(metaData,
				fundTransferRequestDTO);
		assertNull(fundTransferResponseDTO);
	}

	@Test
	public void handleSuccessTest() {
		CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
		coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
		FundTransferResponse fundTransferResponse = FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();

		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(true);

		doNothing().when(digitalUserLimitUsageService).insert(any());
		when(transferLimitService.saveTransferDetails(any(), any())).thenReturn(new TransferLimitResponseDto(true, null,
				null));
		fundTransferServiceDefault.handleIfTransactionIsSuccess(metaData, fundTransferRequestDTO, new UserDTO(), fundTransferResponse);
		verify(digitalUserLimitUsageService,times(1)).insert(any());
		verify(transferLimitService,times(1)).saveTransferDetails(any(), any());
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

		coreFundTransferResponseDto.setMwResponseCode("EAI-FCI-BRK-002");
		FundTransferResponse fundTransferResponse2 = FundTransferResponse.builder().responseDto(coreFundTransferResponseDto).build();

		Assertions.assertThrows(GenericException.class,()-> fundTransferServiceDefault.handleFailure(fundTransferRequestDTO, fundTransferResponse2));

	}

	@Test
	public void transferFundTestOTPFailure() {
		fundTransferRequestDTO.setServiceType("WAMA");
		try {
			TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
					new TwoFactorAuthRequiredCheckResponseDto();
			twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
			//when
			when(service.checkIfTwoFactorAuthenticationRequired(any(),
					any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
			fundTransferServiceDefault.transferFund(metaData,
					fundTransferRequestDTO);
		} catch (GenericException genericException) {
			assertEquals("TN-5016", genericException.getErrorCode());
		}
	}
	
	@Test
	public void transferFundTestSuccessFulTermsAndConditionsVerificationOTPFailure() {
		fundTransferRequestDTO.setServiceType("LOCAL");
		try {
			fundTransferRequestDTO.setTermsAndConditionsAccepted(true);
			fundTransferRequestDTO.setJourneyVersion("V2");
			TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
					new TwoFactorAuthRequiredCheckResponseDto();
			twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
			//when
			when(service.checkIfTwoFactorAuthenticationRequired(any(),
					any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
			fundTransferServiceDefault.transferFund(metaData, fundTransferRequestDTO);
			Mockito.verify(asyncUserEventPublisher, Mockito.times(1)).publishSuccessEvent(
					Mockito.eq(FundTransferEventType.FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED), Mockito.any(),
					Mockito.eq(FundTransferEventType.FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED.getDescription()));
		} catch (GenericException genericException) {
			assertEquals("TN-5016", genericException.getErrorCode());
		}
	}
	
	@Test
	public void transferFundTestNonAcceptedTermsAndConditions() {
		Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		try {
			ReflectionTestUtils.setField(fundTransferServiceDefault, "cprEnabled", true);
			fundTransferRequestDTO.setTermsAndConditionsAccepted(false);
			fundTransferRequestDTO.setJourneyVersion("V2");
			TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
					new TwoFactorAuthRequiredCheckResponseDto();
			twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
			//when
			when(service.checkIfTwoFactorAuthenticationRequired(any(),
					any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
			fundTransferServiceDefault.transferFund(metaData, fundTransferRequestDTO);
			Mockito.verify(asyncUserEventPublisher, Mockito.times(1)).publishFailedEsbEvent(FundTransferEventType.FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED,
					Mockito.any(), CommonConstants.FUND_TRANSFER,Mockito.any(),
                    TransferErrorCode.TERMSANDCONDITIONS_NOTACCEPTED.toString(),
                    TransferErrorCode.TERMSANDCONDITIONS_NOTACCEPTED.getErrorMessage(),
                    TransferErrorCode.TERMSANDCONDITIONS_NOTACCEPTED.getErrorMessage());
		} catch (GenericException genericException) {
			assertEquals(TransferErrorCode.TERMSANDCONDITIONS_NOTACCEPTED.getCustomErrorCode(), genericException.getErrorCode());
		}
	}
	
	@Test
	public void transferFundTestCPRDisabledVerificationOTPFailure() {
		fundTransferRequestDTO.setServiceType("INFT");
		try {
			ReflectionTestUtils.setField(fundTransferServiceDefault, "cprEnabled", true);
			fundTransferRequestDTO.setTermsAndConditionsAccepted(true);
			fundTransferRequestDTO.setJourneyVersion("V2");
			TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
					new TwoFactorAuthRequiredCheckResponseDto();
			twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
			//when
			when(service.checkIfTwoFactorAuthenticationRequired(any(),
					any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
			fundTransferServiceDefault.transferFund(metaData, fundTransferRequestDTO);
			Mockito.verify(asyncUserEventPublisher, Mockito.times(1)).publishSuccessEvent(
					Mockito.eq(FundTransferEventType.FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED), Mockito.any(),
					Mockito.eq(FundTransferEventType.FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED.getDescription()));
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
		strategy = new InternationalFundTransferStrategy(null, null, null, null, null, null, null, null, null, null, null, null,null, null,null);
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
	
	@Test
	public void transferFundTest_withOTPRelaxedLogic() {
		TwoFactorAuthRequiredCheckResponseDto validationCheckResponse = new TwoFactorAuthRequiredCheckResponseDto();
		validationCheckResponse.setTwoFactorAuthRequired(false);
		try {
			ReflectionTestUtils.setField(fundTransferServiceDefault, "cprEnabled", true);
			fundTransferRequestDTO.setTermsAndConditionsAccepted(true);
			fundTransferRequestDTO.setJourneyVersion("V2");
			TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
					new TwoFactorAuthRequiredCheckResponseDto();
			twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
			//when
			when(service.checkIfTwoFactorAuthenticationRequired(any(),
					any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
			fundTransferServiceDefault.transferFund(metaData, fundTransferRequestDTO);
			Mockito.verify(asyncUserEventPublisher, Mockito.times(1)).publishSuccessEvent(
					Mockito.eq(FundTransferEventType.FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED), Mockito.any(),
					Mockito.eq(FundTransferEventType.FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED.getDescription()));
		} catch (GenericException genericException) {
			assertEquals("TN-5016", genericException.getErrorCode());
		}
	}
}
