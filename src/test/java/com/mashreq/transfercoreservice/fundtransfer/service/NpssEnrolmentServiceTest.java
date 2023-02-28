package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NpssEnrolmentServiceTest {

    @InjectMocks
    private NpssEnrolmentService npssEnrolmentService;
    @Mock
    private NpssEnrolmentRepository npssEnrolmentRepository;
    @Mock
    private FundTransferServiceDefault fundTransferServiceDefault;
    @Mock
    private DigitalUserService digitalUserService;
    @Mock
    private DigitalUserLimitUsageService digitalUserLimitUsageService;
    @Mock
    private NpssNotificationService npssNotificationService;
    @Mock
    private AccountService accountService;

    @Test
    public void testEnrolmentEnrolled() {
        RequestMetaData metaData = getMetaData("012960010");
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepoDTO> npssUser = Optional.of(dbResult);
        when(npssEnrolmentRepository.getEnrolmentStatus(any())).thenReturn(npssUser);
        npssEnrolmentService.checkEnrolment(metaData);
    }

/*    @Test
    public void testEnrolmentWithSuccess() {
        RequestMetaData metaData = getMetaData("012960010");
        List<NpssEnrolmentRepoDTO> npssEnrolmentResponse = new ArrayList<>();
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepoDTO> npssUser = Optional.of(dbResult);
        Mockito.when(npssEnrolmentRepository.findAllByIsDefaultAccountUpdated(Mockito.anyBoolean())).thenReturn(npssEnrolmentResponse);
        Mockito.when(npssEnrolmentRepository.save(Mockito.any())).thenReturn(npssUser);
        npssEnrolmentService.updateEnrolment(metaData);
    }*/



  //  @Test
    public void updateDefaultAccountExceptionScenarioTest() {
        RequestMetaData metaData = getMetaData("012960010");
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setCurrency("AED");
        accountDetailsDTO.setSchemeType("SA");
        accountDetailsDTO.setStatus("ACTIVE");
        AccountDetailsDTO accountDetails = new AccountDetailsDTO();
        accountDetails.setCurrency("AED");
        accountDetails.setSchemeType("CA");
        accountDetails.setStatus("ACTIVE");
        when(accountService.getAccountsFromCore(anyString())).thenReturn(Arrays.asList(accountDetailsDTO, accountDetails));
        when(npssEnrolmentRepository.save(any())).thenThrow(new RuntimeException());
      //  assertThrows(GenericException.class,()->npssEnrolmentService.updateDefaultAccount(metaData, false));
    }

    private RequestMetaData getMetaData(String cif) {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif(cif);
        return metaData;
    }
}
