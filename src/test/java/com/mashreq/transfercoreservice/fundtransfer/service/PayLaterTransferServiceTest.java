package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.Assert.assertEquals;

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
import org.springframework.web.util.HtmlUtils;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.InternationalPayLaterFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.LocalFundPayLaterTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.OwnAccountPayLaterStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.paylater.WithinMashreqPayLaterStrategy;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
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
	OTPService iamService;
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
	FundTransferRequestDTO fundTransferRequestDTO;

	@Mock
	CoreFundTransferResponseDto coreFundTransferResponseDto;

	@Before
	public void prepare() {
		this.payLaterTransferService.init();
		fundTransferRequestDTO = generateFundTransferRequest();
	}

	@Test
	public void transferFundTest() {
		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(true);
		Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(Mockito.any(), Mockito.any(),
				Mockito.any());
		Mockito.when(iamService.verifyOTP(Mockito.any())).thenReturn(Response.<VerifyOTPResponseDTO>builder()
				.status(ResponseStatus.SUCCESS).data(verifyOTPResponseDTO).build());
		FundTransferResponseDTO fundTransferResponseDTO = payLaterTransferService.transferFund(metaData,
				fundTransferRequestDTO);
		Assert.assertNull(fundTransferResponseDTO);
	}

	
	@Test
	public void transferFundTestOTPFailure() {
		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(false);
		Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.when(iamService.verifyOTP(Mockito.any()))
				.thenReturn(Response.<VerifyOTPResponseDTO>builder().status(ResponseStatus.FAIL).errorCode("TN-5016")
						.errorDetails("Something went wrong with OTP external service").data(verifyOTPResponseDTO)
						.build());
		try {
			payLaterTransferService.transferFund(metaData, fundTransferRequestDTO);
		} catch (GenericException genericException) {
			assertEquals("TN-5016", genericException.getErrorCode());
		}
	}

	@Test
	public void getFundTransferResponseInvalidCIFTest() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		metaData.setPrimaryCif("123456");
		FundTransferRequestDTO fundTransferRequestDTO = generateFundTransferRequest();
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
		FundTransferRequestDTO fundTransferRequestDTO = generateFundTransferRequest();
		try {
			fundTransferRequestDTO.setServiceType("WYMA");
			fundTransferRequestDTO.setCurrency("AED");
			fundTransferRequestDTO.setTxnCurrency("AED");
			fundTransferRequestDTO.setDealNumber("AE12345");
			//Mockito.when(digitalUserRepository.findByCifEquals(Mockito.eq(metaData.getPrimaryCif()))).thenReturn(Optional.of(createDigitalUserDTO()));
			payLaterTransferService.transferFund(metaData, fundTransferRequestDTO);
		} catch (GenericException e) {
			assertEquals(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getCustomErrorCode(), e.getErrorCode());
		}	}

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
