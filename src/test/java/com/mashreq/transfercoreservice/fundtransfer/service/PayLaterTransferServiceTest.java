package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.EnumMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.InternationalPayLaterFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.LocalFundPayLaterTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.OwnAccountPayLaterStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.WithinMashreqPayLaterStrategy;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

@RunWith(MockitoJUnitRunner.class)
public class PayLaterTransferServiceTest {
	@Mock
	RequestMetaData metaData;
	@Mock
	FundTransferResponseDTO fundTransferResponseDTO;
	@Mock
	VerifyOTPRequestDTO verifyOTPRequestDTO;
	@Mock
	AsyncUserEventPublisher asyncUserEventPublisher;
	@Mock
	DigitalUser digitalUser;
	@InjectMocks
	PayLaterTransferService payLaterTransferService;
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
	InternationalPayLaterFundTransferStrategy internationalPayLaterFundTransferStrategy;
	@Mock
	OwnAccountPayLaterStrategy ownAccountPayLaterStrategy;
	@Mock
	WithinMashreqPayLaterStrategy withinMashreqPayLaterStrategy;
	@Mock
	LocalFundPayLaterTransferStrategy localFundPayLaterTransferStrategy;
	@Mock
	private TwoFactorAuthRequiredCheckService service;
	FundTransferRequestDTO fundTransferRequestDTO;

	@Mock
	CoreFundTransferResponseDto coreFundTransferResponseDto;

	@Before
	public void prepare() {
		this.payLaterTransferService.init();
		fundTransferRequestDTO = generateFundTransferRequest(FTOrderType.PL);
		ReflectionTestUtils.setField(payLaterTransferService, "activeProfile", "prod");
		ReflectionTestUtils.setField(payLaterTransferService, "standingInstructionsDisabled", false);
	}
	
	@Test
	public void transferFundTest() {
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
				new TwoFactorAuthRequiredCheckResponseDto();
		twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
		//when
		when(service.checkIfTwoFactorAuthenticationRequired(any(),
				any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
		Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(Mockito.any(), Mockito.any(),
				Mockito.any());
		FundTransferResponseDTO fundTransferResponseDTO = payLaterTransferService.transferFund(metaData,
				fundTransferRequestDTO);
		Assert.assertNull(fundTransferResponseDTO);
	}

	
	@Test
	public void transferFundTestOTPFailure() {
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
				new TwoFactorAuthRequiredCheckResponseDto();
		twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
		try {
			//when
			when(service.checkIfTwoFactorAuthenticationRequired(any(),
					any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
			payLaterTransferService.transferFund(metaData, fundTransferRequestDTO);
		} catch (GenericException genericException) {
			assertEquals("TN-5016", genericException.getErrorCode());
		}
	}

	@Test
	public void getFundTransferResponseInvalidCIFTest() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		metaData.setPrimaryCif("123456");
		FundTransferRequestDTO fundTransferRequestDTO = generateFundTransferRequest(FTOrderType.PL);
		try {
			fundTransferRequestDTO.setServiceType("WYMA");
			payLaterTransferService.transferFund(metaData, fundTransferRequestDTO);
		} catch (GenericException e) {
			assertEquals(TransferErrorCode.INVALID_CIF.getCustomErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void test_dealnumberNotValid() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		metaData.setPrimaryCif("123456");
		FundTransferRequestDTO fundTransferRequestDTO = generateFundTransferRequest(FTOrderType.PL);
		try {
			fundTransferRequestDTO.setServiceType("WYMA");
			fundTransferRequestDTO.setCurrency("AED");
			fundTransferRequestDTO.setTxnCurrency("AED");
			fundTransferRequestDTO.setDealNumber("AE12345");
			//Mockito.when(digitalUserRepository.findByCifEquals(Mockito.eq(metaData.getPrimaryCif()))).thenReturn(Optional.of(createDigitalUserDTO()));
			payLaterTransferService.transferFund(metaData, fundTransferRequestDTO);
		} catch (GenericException e) {
			assertEquals(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getCustomErrorCode(), e.getErrorCode());
		}	
	}
	
	@Test
	public void transferFundTest_SI() {
		FundTransferRequestDTO fundTransferRequestDTO = generateFundTransferRequest(FTOrderType.SI);
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
				new TwoFactorAuthRequiredCheckResponseDto();
		twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
		//when
		when(service.checkIfTwoFactorAuthenticationRequired(any(),
				any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
		Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(Mockito.any(), Mockito.any(),
				Mockito.any());
		FundTransferResponseDTO fundTransferResponseDTO = payLaterTransferService.transferFund(metaData,
				fundTransferRequestDTO);
		Assert.assertNull(fundTransferResponseDTO);
	}

	@Test
	public void transferFundTest_SI_Disabled() {
		ReflectionTestUtils.setField(payLaterTransferService, "standingInstructionsDisabled", true);
		FundTransferRequestDTO fundTransferRequestDTO = generateFundTransferRequest(FTOrderType.SI);
		
		try {
			payLaterTransferService.transferFund(metaData, fundTransferRequestDTO);
		} catch (GenericException e) {
			assertEquals(TransferErrorCode.PAY_LATER_TRANSACTION_INITIATION_FAILED.getCustomErrorCode(), e.getErrorCode());
		}
	}
	
	
	private FundTransferRequestDTO generateFundTransferRequest(FTOrderType orderTpe) {
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
		fundTransferRequestDTO.setOrderType(orderTpe.getName());
		return fundTransferRequestDTO;
	}

}
