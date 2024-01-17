package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mashreq.transfercoreservice.notification.service.EmailUtil.ONLINE_BANKING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * @author ThanigachalamP
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private PushNotificationImpl pushNotification;

    @Mock
    private AsyncUserEventPublisher userEventPublisher;

    @Mock
    private DigitalUserSegment digitalUserSegment;

    @Mock
    private SMSService smsService;

    @BeforeEach
    public void init(){

    }

    @Test
    public void sendNotificationsTest() {
        when(digitalUserSegment.getCustomerCareInfo(any()))
                .thenReturn(new Segment());
        when(smsService.sendSMS(any(),any(),any(),anyInt(),any())).thenReturn(true);
        CustomerNotification customerNotification = new CustomerNotification();
        notificationService.sendNotifications(customerNotification,
                "PAYMENT_REQUEST_SENT_MULTIPLE_RTP",RequestMetaData.builder().mobileNUmber("12345678").channel("").build(), new UserDTO());
        assertEquals(ONLINE_BANKING, customerNotification.getChannel());
    }

    @Test
    public void sendNotificationsWhenSmsFailedTest() {
        when(digitalUserSegment.getCustomerCareInfo(any()))
                .thenReturn(new Segment());
        when(smsService.sendSMS(any(),any(),any(),anyInt(),any())).thenThrow(new RuntimeException());
        CustomerNotification customerNotification = new CustomerNotification();
        notificationService.sendNotifications(customerNotification,
                "PAYMENT_REQUEST_SENT_MULTIPLE_RTP",RequestMetaData.builder().mobileNUmber("12345678").channel("").build(), new UserDTO());
        assertEquals(ONLINE_BANKING, customerNotification.getChannel());
    }

    @Test
    public void sendNotificationsPushNotificationSuccessTest() {
        when(digitalUserSegment.getCustomerCareInfo(any()))
                .thenReturn(new Segment());
        when(smsService.sendSMS(any(),any(),any(),anyInt(),any())).thenReturn(true);
        when(pushNotification.sendPushNotification(any(),any(),any(),any())).thenReturn(true);
        CustomerNotification customerNotification = new CustomerNotification();
        notificationService.sendNotifications(customerNotification,
                "PAYMENT_REQUEST_SENT_MULTIPLE_RTP",RequestMetaData.builder().mobileNUmber("12345678").channel("").build(), new UserDTO());
        assertEquals(ONLINE_BANKING, customerNotification.getChannel());
    }

    @Test
    public void sendNotificationsPushNotificationFailedTest() {
        when(digitalUserSegment.getCustomerCareInfo(any()))
                .thenReturn(new Segment());
        when(smsService.sendSMS(any(),any(),any(),anyInt(),any())).thenReturn(true);
        when(pushNotification.sendPushNotification(any(),any(),any(),any())).thenThrow(new RuntimeException());
        CustomerNotification customerNotification = new CustomerNotification();
        notificationService.sendNotifications(customerNotification,
                "PAYMENT_REQUEST_SENT_MULTIPLE_RTP",RequestMetaData.builder().mobileNUmber("12345678").channel("").build(), new UserDTO());
        assertEquals(ONLINE_BANKING, customerNotification.getChannel());
    }
}
