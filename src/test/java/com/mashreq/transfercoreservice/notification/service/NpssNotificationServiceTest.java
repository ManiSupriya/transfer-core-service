
package com.mashreq.transfercoreservice.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.templates.freemarker.TemplateEngine;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.service.NpssNotificationService;
import com.mashreq.transfercoreservice.notification.model.*;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


/**
 * @author ThanigachalamP
 */

@ExtendWith(MockitoExtension.class)
public class NpssNotificationServiceTest {
    @InjectMocks
    private NpssNotificationService npssNotificationService;

    @Mock
    private EmailConfig emailConfig;


    @Mock
    private AsyncUserEventPublisher asyncUserEventPublisher;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private TemplateEngine templateEngine;

    @BeforeEach
    public void init(){

    }

    @Test
    public void performPostTransactionActivitiesTest() throws JsonProcessingException {
        HashMap<String, EmailParameters> emailMap = new HashMap<>();
        Map<String, String> socialMediaLinks = new HashMap<>();
        socialMediaLinks.put("facebook","");

        EmailParameters emailParameters = new EmailParameters();
        emailParameters.setEnrolmentConfirmSubject("Customer Enroled Successfully");
        emailParameters.setEnrolmentConfirm("templates/enrolmentConfirm");
        EmailTemplateParameters emailTemplateParameters = EmailTemplateParameters.builder()
                .socialMediaLinks(socialMediaLinks)
                .channelIdentifier(ChannelDetails.builder().channelName("WEB").build()).build();
        emailMap.put("AE",emailParameters);
      /*  when(emailConfig.getEmail()).thenReturn(emailMap);
        when(emailUtil.getEmailTemplateParameters(any(),any())).thenReturn(emailTemplateParameters);
        when(templateEngine.generate(any())).thenReturn("");*/
        npssNotificationService.performPostTransactionActivities(RequestMetaData.builder().country("AE").email("bhuvi@wertyg").mobileNUmber("12345678").channel("").build(),
                NotificationRequestDto.builder().notificationType(NotificationType.CUSTOMER_ENROLMENT).build());
    }
}

