package com.mashreq.transfercoreservice.config.notification;

import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.EmailUtil;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class SMSConfigTest {

    @InjectMocks
    SMSConfig smsConfig = new SMSConfig();
    @InjectMocks
    EmailUtil emailUtil = new EmailUtil();

    @Test
    void getSMSTemplate() {
        ReflectionTestUtils.setField(
                smsConfig,
                "plSiCreation",
                "On your request SI/Pay later has been set up for the beneficiary {0}, " +
                        "account number {1}. If not requested, please contact {2}. " +
                        "Thank you for using our services.");
        ReflectionTestUtils.setField(
                smsConfig,
                "ownAccountTransactionInitiated",
                "Request received for Fund Transfer of {0} {1}" +
                        " with Reference number {2}. Contact {3} if you did not initiate this.");
        ReflectionTestUtils.setField(smsConfig, "emailUtil", emailUtil);
        Segment segment = new Segment();
        CustomerNotification customerNotification = new CustomerNotification();
        customerNotification.setCreditAccount("AE1234567890");
        customerNotification.setBeneficiaryName("NAWAZ");
        segment.setCustomerCareNumber("+971 44335566");
        customerNotification.setSegment(segment);
        customerNotification.setCurrency("AED");
        customerNotification.setAmount("10,000.00");
        customerNotification.setTxnRef("ABCDEFG");
        customerNotification.setSegment(segment);
        String response = smsConfig.getSMSTemplate("PL CREATION", customerNotification);
        assertNotNull(response);
        String response2 = smsConfig.getSMSTemplate("SI CREATION", customerNotification);
        assertNotNull(response2);
    }
}