package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.model.DigitalUserGroup;
import com.mashreq.transfercoreservice.model.Segment;

public class FundTransferTestUtil {

	public static DigitalUser createDigitalUserDTO() {
		DigitalUser digUser = new DigitalUser();
		digUser.setCif("012345678");
		digUser.setCountry("AE");
		digUser.setDeviceidRegisteredForPushnotify("fcwrdr142516");
		DigitalUserGroup digitalUserGroup = new DigitalUserGroup();
		Segment segment = new Segment();
		segment.setId(2l);
		digitalUserGroup.setSegment(segment );
		Country country = new Country();
		country.setLocalCurrency("AED");
		digitalUserGroup.setCountry(country );
		digUser.setDigitalUserGroup(digitalUserGroup );
		digUser.setDeviceInfo("Mobile");
        return digUser;
    }
	
	public static RequestMetaData getMetadata() {
		RequestMetaData metadata = new RequestMetaData();
		metadata.setPrimaryCif("012345678");
		return metadata;
	}
	
	public static UserDTO getUserDTO() {
		RequestMetaData fundTransferMetadata = getMetadata();
		UserDTO userDTO = new UserDTO();
		DigitalUser digitalUser = createDigitalUserDTO();
        userDTO.setCifId(fundTransferMetadata.getPrimaryCif());
        userDTO.setUserId(digitalUser.getId());
        userDTO.setSegmentId(digitalUser.getDigitalUserGroup().getSegment().getId());
        userDTO.setCountryId(digitalUser.getDigitalUserGroup().getCountry().getId());
        userDTO.setLocalCurrency(digitalUser.getDigitalUserGroup().getCountry().getLocalCurrency());
        userDTO.setDeviceRegisteredForPush(digitalUser.getDeviceidRegisteredForPushnotify());
        userDTO.setDeviceInfo(digitalUser.getDeviceInfo());
        return userDTO;
	}
	
	public static FundTransferRequestDTO generateFundTransferRequest() {
		FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
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

	public static List<AccountDetailsDTO> generateAccountsList(RequestMetaData metadata,
			FundTransferRequestDTO request) {
		List<AccountDetailsDTO> accountsList = new ArrayList<>();
		AccountDetailsDTO dto = new AccountDetailsDTO();
		dto.setNumber(request.getFromAccount());
		dto.setCurrency(request.getCurrency());
		accountsList.add(dto);
		dto = new AccountDetailsDTO();
		dto.setNumber(request.getToAccount());
		dto.setCurrency(request.getTxnCurrency());
		accountsList.add(dto);
		return accountsList;
	}
	
	public static CurrencyConversionDto getConversionResult(FundTransferRequestDTO request) {
		CurrencyConversionDto dto = new CurrencyConversionDto();
		dto.setAccountCurrencyAmount(request.getAmount());
		dto.setExchangeRate(BigDecimal.valueOf(2d));
		return dto;
	}
}
