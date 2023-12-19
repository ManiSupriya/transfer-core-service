package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.config.notification.PushTemplate;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mashreq.transfercoreservice.util.TestUtil.getCustomerNotification;
import static com.mashreq.transfercoreservice.util.TestUtil.getNotificationResponse;
import static com.mashreq.transfercoreservice.util.TestUtil.getRequestMetadata;
import static com.mashreq.transfercoreservice.util.TestUtil.getUserDto;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by KrishnaKo on 19/12/2023
 */
@ExtendWith(MockitoExtension.class)
public class PushNotificationImplTest {

    @InjectMocks
    PushNotificationImpl pushNotification;

    @Mock
    NotificationClient notificationClient;
    @Mock
    PushTemplate pushTemplate;


    @Test
    void testSendPushNotification(){
        when(notificationClient.sendPushNotification(anyMap(),any(),anyString())).thenReturn(getNotificationResponse());
        when(pushTemplate.getPushTemplate(anyString(),any())).thenReturn("Message");
        assertTrue(pushNotification.sendPushNotification(getCustomerNotification(),"PL CREATION",getRequestMetadata(),getUserDto()));
    }
    @Test
    void testSendPushNotificationDevicePushFalse(){
        UserDTO userDTO = getUserDto();
        userDTO.setDeviceRegisteredForPush(null);
        assertFalse(pushNotification.sendPushNotification(getCustomerNotification(),"PL CREATION",getRequestMetadata(),userDTO));
    }

}
