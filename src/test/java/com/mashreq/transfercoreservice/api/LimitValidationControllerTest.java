package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.LimitValidatorRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class LimitValidationControllerTest {

	@Mock
	private LimitValidator limitValidator;
	
	private LimitValidationController limitValidationController;

	@BeforeEach
	public void init() {
		limitValidationController = new LimitValidationController(limitValidator);
	}
	
	@Test()
	public void test_validateLimit() {
		RequestMetaData metaData = getMetaData();
		//when(limitValidator.validate(any(),anyString(),new BigDecimal(5),any(),anyLong())).thenReturn(getLimitValidatorResponse());
		limitValidationController.validateLimit(metaData , new LimitValidatorRequestDto());
	}

	private LimitValidatorResponse getLimitValidatorResponse() {
		LimitValidatorResponse limitValidatorResponse = new LimitValidatorResponse();
		return limitValidatorResponse;
	}

	private RequestMetaData getMetaData() {
		RequestMetaData metaData = new RequestMetaData();
		metaData.setPrimaryCif("22231312");
		return metaData;
	}

}
