
package com.mashreq.transfercoreservice.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.notification.client.notification.service.NotificationService;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.service.TransferBankChargesService;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.*;
import com.mashreq.transfercoreservice.notification.service.EmailUtil;
import com.mashreq.transfercoreservice.notification.service.PostTransactionActivityService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.notification.service.SendEmailActivity;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.INFT_PL_SI_CREATION;
import static com.mashreq.transfercoreservice.notification.service.EmailUtil.*;
import static com.mashreq.transfercoreservice.util.TestUtil.getBeneficiaryDto;
import static java.lang.Long.valueOf;
import static java.util.Optional.ofNullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


/**
 * @author ThanigachalamP
 */

@ExtendWith(MockitoExtension.class)
public class PostTransactionServiceTest {
    @Mock
    private PostTransactionActivityService postTransactionActivityService;

    @Mock
    private SendEmailActivity sendEmailActivity;

    @Mock
    private AsyncUserEventPublisher userEventPublisher;

    @Mock
    private EmailUtil emailUtil;
    @Mock
    private NotificationService notificationService;

    @Mock
    private BeneficiaryService beneficiaryService;
    @Mock
    private TransferBankChargesService transferBankChargesService;

    @InjectMocks
    private PostTransactionService postTransactionService;

    private final String CC_NO = "E6BD9127E95D80C2C0D46DB2A314514C315A21C8408729F99ECA3D22D123DB2D";

    private static final String SOURCE_OF_FUND_CC = "Credit Card";


    @BeforeEach
    public void init(){


        //ReflectionTestUtils.setField(postTransactionService,"emailConfig", emailConfig);
       // ReflectionTestUtils.setField(postTransactionService,"postTransactionActivityService", postTransactionActivityService);
       // ReflectionTestUtils.setField(postTransactionService,"sendEmailActivity", sendEmailActivity);
       // ReflectionTestUtils.setField(postTransactionService,"userEventPublisher", userEventPublisher);
        ReflectionTestUtils.setField(postTransactionService,"defaultLanguage","EN");
    }

    private FundTransferRequest buildFundTransferRequest(){
        FundTransferRequest fundTransferRequest = FundTransferRequest.builder().build();
        fundTransferRequest.setSourceOfFund(SOURCE_OF_FUND_CC);
        fundTransferRequest.setNotificationType(NotificationType.LOCAL);
        fundTransferRequest.setTransferType(ServiceType.LOCAL.getName());
        fundTransferRequest.setFromAccount(CC_NO);
        fundTransferRequest.setStatus(MwResponseStatus.S.getName());
        fundTransferRequest.setServiceType("LOCAL");
        return fundTransferRequest;
    }

    private RequestMetaData buildRequestMetaData(){
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setPrimaryCif("012441750");
        requestMetaData.setChannel("MOB");
        requestMetaData.setUsername("TEST_CUSTOMER");
        requestMetaData.setCountry("AE");
        requestMetaData.setEmail("thanigachalamp@mashreq.com");
        requestMetaData.setSegment("NEO");
        return requestMetaData;
    }

    private EmailTemplateParameters buildEmailTemplateParameters(){
        EmailTemplateParameters emailTemplateParameters = EmailTemplateParameters.builder().build();
        Map<String, String> socialMediaLinks = new HashMap<>();
        socialMediaLinks.put(FACEBOOK, FACEBOOK);
        socialMediaLinks.put(INSTAGRAM, INSTAGRAM);
        socialMediaLinks.put(TWITTER, TWITTER);
        socialMediaLinks.put(LINKED_IN, LINKED_IN);
        socialMediaLinks.put(YOUTUBE, YOUTUBE);
        emailTemplateParameters.setSocialMediaLinks(socialMediaLinks);
        emailTemplateParameters.setChannelIdentifier(buildChannelDetails());
        emailTemplateParameters.setHtmlContactContents(buildContactWebsiteContent());
        emailTemplateParameters.setSegment(new Segment());
        return emailTemplateParameters;
    }

    private EmailTemplateContactWebsiteContent buildContactWebsiteContent(){
        EmailTemplateContactWebsiteContent emailTemplateContactWebsiteContent = EmailTemplateContactWebsiteContent.builder().build();
        emailTemplateContactWebsiteContent.setHtmlContent(" Html Content");
        emailTemplateContactWebsiteContent.setSegment("NEO");
        return emailTemplateContactWebsiteContent;
    }

    private ChannelDetails buildChannelDetails(){
        ChannelDetails channelDetails = ChannelDetails.builder().build();
        channelDetails.setChannelName("MOB");
        return channelDetails;
    }

    private EmailParameters buildEmailParameters(){
        EmailParameters emailParameters = new EmailParameters();
        emailParameters.setEmailSubject("Email Subject");
        emailParameters.setLocalFundTransfer("localFundTransfer");
        return emailParameters;
    }

    @Test
    public void testPostTransactionService() throws JsonProcessingException {
        RequestMetaData requestMetaData = buildRequestMetaData();
        FundTransferRequest fundTransferRequest = buildFundTransferRequest();
        fundTransferRequest.setChargeBearer("B");
        postTransactionService.performPostTransactionActivities(requestMetaData, fundTransferRequest, new FundTransferRequestDTO(), buildBeneficiary());
    }

    private Optional<BeneficiaryDto> buildBeneficiary(){
       return Optional.of(new BeneficiaryDto());
    }
    @Test
    public void testPostTransactionServiceWithSourceAmount() throws JsonProcessingException {
        RequestMetaData requestMetaData = buildRequestMetaData();

        FundTransferRequest fundTransferRequest = buildFundTransferRequest();
        fundTransferRequest.setSrcAmount(new BigDecimal(10));
        fundTransferRequest.setServiceType("LOCAL");
        fundTransferRequest.setNotificationType("LOCAL");
        fundTransferRequest.setChargeBearer("O");

        postTransactionService.performPostTransactionActivities(requestMetaData, fundTransferRequest, new FundTransferRequestDTO(), buildBeneficiary());
    }

    @Test
    public void testPostTransactionServiceWithTxnAmount() throws JsonProcessingException {
        RequestMetaData requestMetaData = buildRequestMetaData();

        FundTransferRequest fundTransferRequest = buildFundTransferRequest();
        fundTransferRequest.setAmount(new BigDecimal(10));
        fundTransferRequest.setNotificationType(INFT_PL_SI_CREATION);
        fundTransferRequest.setServiceType("INFT");
        fundTransferRequest.setChargeBearer("U");

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1");
        postTransactionService.performPostTransactionActivities(requestMetaData, fundTransferRequest, fundTransferRequestDTO, buildBeneficiary());
    }
    
    //postTransactionActivityService
    
    @Test
    public void testPostTransactionServiceWithWAMA() throws JsonProcessingException {
        RequestMetaData requestMetaData = buildRequestMetaData();

        FundTransferRequest fundTransferRequest = buildFundTransferRequest();
        fundTransferRequest.setAmount(new BigDecimal(10));
        fundTransferRequest.setNotificationType(INFT_PL_SI_CREATION);
        fundTransferRequest.setServiceType("WAMA");

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1");
        doNothing().when(notificationService).sendNotification(any());
        postTransactionService.performPostTransactionActivities(requestMetaData, fundTransferRequest, fundTransferRequestDTO, buildBeneficiary());
    }
    
    @Test
    public void testPostTransactionServiceWithWYMA() throws JsonProcessingException {
        RequestMetaData requestMetaData = buildRequestMetaData();

        FundTransferRequest fundTransferRequest = buildFundTransferRequest();
        fundTransferRequest.setAmount(new BigDecimal(10));
        fundTransferRequest.setNotificationType(INFT_PL_SI_CREATION);
        fundTransferRequest.setServiceType("WYMA");

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1");

        EmailParameters emailParameters = buildEmailParameters();
        emailParameters.setPlSiFundTransfer("plSiFundTransfer");

        doNothing().when(notificationService).sendNotification(any());
        postTransactionService.performPostTransactionActivities(requestMetaData, fundTransferRequest, fundTransferRequestDTO, buildBeneficiary());
    }
}