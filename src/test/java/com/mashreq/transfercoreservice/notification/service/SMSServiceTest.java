package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.notification.client.notification.service.NotificationService;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.config.notification.SMSConfig;
import com.mashreq.transfercoreservice.notification.model.SMSResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static com.mashreq.transfercoreservice.util.TestUtil.getCustomerNotification;
import static com.mashreq.transfercoreservice.util.TestUtil.getRequestMetadata;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Created by KrishnaKo on 19/12/2023
 */
@ExtendWith(MockitoExtension.class)
public class SMSServiceTest {

    @InjectMocks
    SMSService smsService;

    @Mock
    NotificationClient notificationClient;

    @Mock
    SMSConfig smsConfig;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    public void init(){
        ReflectionTestUtils.setField(smsService,"defaultLanguage","EN");
    }
    @Test
    void testSendSmsForPL(){
         doNothing().when(notificationService).sendNotification(any());
        assertTrue(smsService.sendSMS(getCustomerNotification(),"PL CREATION",getRequestMetadata(),0, "058882211111"));
    }
    @Test
    void testSendSmsForPLExceptionScenario(){
        doThrow(GenericException.class).when(notificationService).sendNotification(any());
        assertTrue(smsService.sendSMS(getCustomerNotification(),"PL CREATION",getRequestMetadata(),0, "058882211111"));
    }
    @Test
    void testSendSmsForFundsTransfer(){
        doNothing().when(notificationService).sendNotification(any());
        assertTrue(smsService.sendSMS(getCustomerNotification(),"FUNDS TRANSFER",getRequestMetadata(),0, "058882211111"));
    }
    @Test
    void testSendSmsForFundsTransferExceptionScenario(){
        doThrow(GenericException.class).when(notificationService).sendNotification(any());
        assertTrue(smsService.sendSMS(getCustomerNotification(),"FUNDS TRANSFER",getRequestMetadata(),0, "058882211111"));
    }
    @Test
    void testSendSmsForOthers(){
        SMSResponse smsResponse = new SMSResponse();
        smsResponse.setStatusCode("SUCCESS");
        smsResponse.setStatusDescription("Sent Successfully");
        when(notificationClient.sendSMS(anyMap(),any())).thenReturn(smsResponse);
        assertTrue(smsService.sendSMS(getCustomerNotification(),"OTHERS",getRequestMetadata(),0, "058882211111"));
    }

    @Test
    void testSendSmsForOthersResponseStatusCodeEmpty(){
        SMSResponse smsResponse = new SMSResponse();
        when(notificationClient.sendSMS(anyMap(),any())).thenReturn(smsResponse);
        assertFalse(smsService.sendSMS(getCustomerNotification(),"OTHERS",getRequestMetadata(),0, "058882211111"));
    }

    @Test
    void testSendSmsForOthersResponseNull(){
        when(notificationClient.sendSMS(anyMap(),any())).thenReturn(null);
        assertFalse(smsService.sendSMS(getCustomerNotification(),"OTHERS",getRequestMetadata(),0, "058882211111"));
    }

}
