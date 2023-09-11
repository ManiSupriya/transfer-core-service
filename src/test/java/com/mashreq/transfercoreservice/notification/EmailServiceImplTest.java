/*
package com.mashreq.transfercoreservice.notification;

import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.config.notification.EmailTemplateHelper;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.notification.model.EmailRequest;
import com.mashreq.transfercoreservice.notification.model.EmailResponse;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import com.mashreq.transfercoreservice.notification.service.EmailServiceImpl;
import freemarker.template.TemplateException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.mockito.Mockito.*;


*/
/**
 * @author ThanigachalamP
 *//*

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceImplTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private EmailTemplateHelper emailTemplateHelper;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Before
    public void init(){

    }

    private SendEmailRequest buildSendEmailRequest(){
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder().build();
        sendEmailRequest.setFromEmailAddress("mashreqdigital@mashreq.com");
        sendEmailRequest.setToEmailAddress("xxx@mashreq.com");
        sendEmailRequest.setFromEmailName("XXX");
        sendEmailRequest.setSubject("FT");
        return sendEmailRequest;
    }

    @Test
    public void testPrepareEmailRequest() throws IOException, TemplateException {
        SendEmailRequest sendEmailRequest = buildSendEmailRequest();
        //when(emailTemplateHelper.getEmailTemplate(anyString(),anyMap())).thenReturn("Sample Body");
        EmailRequest emailRequest =  emailService.prepareEmailRequest(sendEmailRequest);
        Assert.assertEquals(emailRequest.getToEmailAddress(), sendEmailRequest.getToEmailAddress());
    }

    @Test
    public void testSendMessage()  {
        EmailResponse emailResponse = new EmailResponse();
        SendEmailRequest emailRequest = SendEmailRequest.builder().fromEmailAddress("").build();
        ResponseEntity<EmailResponse> emailResponseResponseEntity = new ResponseEntity<>(emailResponse,HttpStatus.OK);
        emailResponseResponseEntity.getBody().setSuccess(true);
        when(notificationClient.sendEmail(any())).thenReturn(emailResponseResponseEntity);
        EmailResponse actualEmailResponse =  emailService.sendMessage(emailRequest, new RequestMetaData());
        Assert.assertNotNull(actualEmailResponse);
        Assert.assertTrue(actualEmailResponse.isSuccess());
    }

    @Test
    public void testSendMessageError()  {
        SendEmailRequest emailRequest = SendEmailRequest.builder().fromEmailAddress("").build();
        when(notificationClient.sendEmail(any())).thenThrow(GenericException.class);
        Throwable exception = Assertions.assertThrows(Exception.class, () -> emailService.sendMessage(emailRequest, new RequestMetaData()));
        Assert.assertEquals(exception.getMessage(), TransferErrorCode.EMAIL_NOTIFICATION_FAILED.getErrorMessage());
    }
}
*/
