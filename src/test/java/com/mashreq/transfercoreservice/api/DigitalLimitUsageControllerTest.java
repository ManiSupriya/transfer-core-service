package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.model.DigitalUser;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class DigitalLimitUsageControllerTest {

	@Mock
	private DigitalUserService digitalUserService;
	@Mock
	private DigitalUserLimitUsageService digitalUserLimitUsageService;
	
	private DigitalLimitUsageController digitalLimitUsageController;

	@BeforeEach
	public void init() {
		digitalLimitUsageController = new DigitalLimitUsageController(digitalUserService,digitalUserLimitUsageService);
	}
	
	@Test()
	public void test_saveDigitalLimitUsage() {
		RequestMetaData metaData = getMetaData();
		when(digitalUserService.getDigitalUser(any())).thenReturn(getDigitalUser());
		doNothing().when(digitalUserLimitUsageService).insert(any());
		digitalLimitUsageController.saveDigitalLimitUsage(metaData , new DigitalUserLimitUsageDTO());
	}

	private DigitalUser getDigitalUser() {
		DigitalUser digitalUser = new DigitalUser();
		digitalUser.setCif("22231312");
		return digitalUser;
	}

	private RequestMetaData getMetaData() {
		RequestMetaData metaData = new RequestMetaData();
		metaData.setPrimaryCif("22231312");
		return metaData;
	}

}
