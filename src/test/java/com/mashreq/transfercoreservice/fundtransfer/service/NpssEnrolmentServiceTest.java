package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.dto.HandleNotificationRequestDto;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentStatusResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentUpdateResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    private TransactionHistoryService transactionHistoryService;

    @Test
    public void testEnrolmentEnrolled() {
        RequestMetaData metaData = getMetaData("012960010");
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepoDTO> npssUser = Optional.of(dbResult);
        when(npssEnrolmentRepository.getEnrolmentStatus(any())).thenReturn(npssUser);
        NpssEnrolmentStatusResponseDTO npssEnrolmentStatusResponseDTO = npssEnrolmentService.checkEnrolment(metaData);
        assertFalse(npssEnrolmentStatusResponseDTO.isAskForEnrolment());
    }

    @Test
    public void testEnrolmentEnrolledWhenEnrollmentStatusNotFound() {
        RequestMetaData metaData = getMetaData("012960010");
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        when(npssEnrolmentRepository.getEnrolmentStatus(any())).thenReturn(Optional.empty());
        NpssEnrolmentStatusResponseDTO npssEnrolmentStatusResponseDTO = npssEnrolmentService.checkEnrolment(metaData);
        assertTrue(npssEnrolmentStatusResponseDTO.isAskForEnrolment());
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


    @Test
    public void updateEnrolmentTest() {
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
        NpssEnrolmentUpdateResponseDTO npssEnrolmentUpdateResponseDTO = npssEnrolmentService.updateEnrolment(metaData);
        assertTrue(npssEnrolmentUpdateResponseDTO.isUserEnrolmentUpdated());
    }
    @Test
    public void updateDefaultAccountTest() {
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
        String result = npssEnrolmentService.updateDefaultAccount(metaData, false,10);
        assertEquals("Data Saved Successfully", result);
    }

    @Test
    public void updateDefaultAccountFromSchedulerTest() {
        RequestMetaData metaData = getMetaData("012960010");
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setCurrency("AED");
        accountDetailsDTO.setSchemeType("SA");
        accountDetailsDTO.setStatus("ACTIVE");
        AccountDetailsDTO accountDetails = new AccountDetailsDTO();
        accountDetails.setCurrency("AED");
        accountDetails.setSchemeType("CA");
        accountDetails.setStatus("ACTIVE");
        when(npssEnrolmentRepository.findAllByIsDefaultAccountUpdated(anyBoolean(),anyInt(),anyInt())).thenReturn(Arrays.asList(NpssEnrolmentRepoDTO.builder().build()));
        String result = npssEnrolmentService.updateDefaultAccount(metaData, true,10);
        assertEquals("Data Saved Successfully", result);
    }

    @Test
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
        String result = npssEnrolmentService.updateDefaultAccount(metaData, false,10);
        assertEquals("Data Saved Successfully", result);
    }

    @Test
    public void handleTransactionTest() {
        RequestMetaData metaData = getMetaData("012960010");
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepoDTO> npssUser = Optional.of(dbResult);
        npssEnrolmentService.handleTransaction(metaData, HandleNotificationRequestDto.builder()
                .notificationRequestDto(NotificationRequestDto.builder().notificationType(NotificationType.PAYMENT_SUCCESS).build()).build());
    }

    @Test
    public void performNotificationActivitiesTest() {
        npssEnrolmentService.performNotificationActivities(RequestMetaData.builder().build(), NotificationRequestDto.builder().build());
    }

    private RequestMetaData getMetaData(String cif) {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif(cif);
        return metaData;
    }
}
