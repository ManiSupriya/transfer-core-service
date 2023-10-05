package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.notification.client.freemarker.TemplateRequest;
import com.mashreq.notification.client.freemarker.TemplateType;
import com.mashreq.notification.client.notification.service.NotificationService;
import com.mashreq.templates.freemarker.TemplateEngine;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.dto.RecipientDTO;
import com.mashreq.transfercoreservice.dto.RtpNotification;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateParameters;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.service.EmailUtil;
import com.mashreq.transfercoreservice.notification.service.PostTransactionActivityService;
import com.mashreq.transfercoreservice.notification.service.SendEmailActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.*;
import static com.mashreq.transfercoreservice.notification.service.EmailUtil.*;

/**
 * Created by KrishnaKo on 24/11/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NpssNotificationService {
    private final NotificationService notificationService;
    private final EmailConfig emailConfig;
    private final AsyncUserEventPublisher userEventPublisher;
    private final DigitalUserService digitalUserService;
    private final EmailUtil emailUtil;


    @Async("generalTaskExecutor")
    public void performPostTransactionActivities(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto) {
        log.info("NpssNotificationService >> performPostTransactionActivities >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        try {
            log.info("NpssNotificationService >> getNewEmailPostTransactionActivityContext Initiated >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            com.mashreq.notification.client.freemarker.TemplateRequest templateRequest = getNewEmailPostTransactionActivityContext(requestMetaData, notificationRequestDto);
            log.info("NpssNotificationService >> getNewEmailPostTransactionActivityContext Completed >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            notificationService.sendNotification(templateRequest);
            log.info("NpssNotificationService >> publishSuccessEvent Completed >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            userEventPublisher.publishSuccessEvent(eventType, requestMetaData, eventType.getDescription());
        } catch (Exception exception) {
            log.error("NpssNotificationService >> publishFailureEvent Completed >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            GenericExceptionHandler.logOnly(exception, transferErrorCode.getErrorMessage());
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }
    }
    public void performNotificationActivities(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto ,UserDTO userDTO) {
        if(PAYMENT_REQUEST_SENT.equalsIgnoreCase(notificationRequestDto.getNotificationType())){
            log.info("NpssNotificationService >> performNotificationActivities >> for Cif {} {}",requestMetaData.getPrimaryCif(),PAYMENT_REQUEST_SENT);
           List<RtpNotification> notificationRequestDtoList = notificationRequestDto.getRtpNotificationList().stream().map(this::mapRtpToNotificationRequest).collect(Collectors.toList());
            notificationRequestDto.setRtpNotificationList(notificationRequestDtoList);
            performSendNotifications(requestMetaData,notificationRequestDto,userDTO);
        }else{
            log.info("NpssNotificationService >> performNotificationActivities >> for Cif for non payment request sent requests{}",requestMetaData.getPrimaryCif());
            performSendNotifications(requestMetaData,  notificationRequestDto , userDTO);
        }

    }

    private RtpNotification mapRtpToNotificationRequest(RtpNotification notificationRequestDto) {
        return RtpNotification.builder().amount(notificationRequestDto.getAmount())
                .contactName(notificationRequestDto.getContactName())
                .sentTo(notificationRequestDto.getSentTo())
                .proxy(StringUtils.isNotEmpty(notificationRequestDto.getProxy()) ? notificationRequestDto.getProxy() : PROXY).build();
    }

    private void performSendNotifications(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto, UserDTO userDTO) {
        log.info("NpssNotificationService >> performSendNotifications >> Initiated cif {} ,request {}", requestMetaData.getPrimaryCif(), notificationRequestDto.toString());
        sendSMSNotification(notificationRequestDto, requestMetaData);
        log.info("NpssNotificationService >> performPostTransactionActivities >> sendSMSNotification call completed cif {}", requestMetaData.getPrimaryCif());
        performPostTransactionActivities(requestMetaData, notificationRequestDto);
        log.info("NpssNotificationService >> performPostTransactionActivities >> Completed {}", notificationRequestDto);
    }

    /*New Email template changes starte */
    private com.mashreq.notification.client.freemarker.TemplateRequest getNewEmailPostTransactionActivityContext(RequestMetaData requestMetaData,NotificationRequestDto notificationRequestDto) throws Exception {
        log.info("NpssNotificationService >> getNewEmailPostTransactionActivityContext >> template formation started: {}",
                htmlEscape(requestMetaData.getUsername()));
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        boolean isMobile = requestMetaData.getChannel().contains(MOBILE);
        String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
        if (StringUtils.isNotBlank(requestMetaData.getEmail())) {
        String templateName = getTemplateName(notificationRequestDto.getNotificationType());

            com.mashreq.notification.client.freemarker.TemplateRequest.EmailBuilder template = buildEmailTemplate(templateName, requestMetaData, channelType)
                    .params(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR))
                    .params(CUSTOMER_NAME, StringUtils.defaultIfBlank(emailUtil.capitalizeFully(requestMetaData.getUsername()), CUSTOMER))
                    .params(CHANNEL_TYPE, StringUtils.defaultIfBlank(channelType, DEFAULT_STR))
                    .params(EMAIL_TEMPLATE_COPYRIGHT_YEAR_KEY, String.valueOf(LocalDateTime.now().getYear()));
            getNewTemplateValuesForNotificationBuilder(template, notificationRequestDto,requestMetaData);

            return template.configure();
        } else {
            log.error("NpssNotificationService >> getNewEmailPostTransactionActivityContext >>emailId/template not found cif: {}",
                    requestMetaData.getPrimaryCif());
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }
        return null;
    }
    private void getNewTemplateValuesForNotificationBuilder(com.mashreq.notification.client.freemarker.TemplateRequest.EmailBuilder builder , NotificationRequestDto notificationRequestDto,RequestMetaData requestMetaData) {
        log.info("NpssNotificationService >> getNewTemplateValuesForNotificationBuilder >> template notification builder started: {}",
                htmlEscape(requestMetaData.getUsername()));

        builder.params(AMOUNT, Optional.ofNullable(notificationRequestDto.getAmount()).isPresent() ? EmailUtil.formattedAmount(notificationRequestDto.getAmount()) : ZERO);
        builder.params(SENT_TO, StringUtils.isNotEmpty(notificationRequestDto.getSentTo()) ? notificationRequestDto.getSentTo() : requestMetaData.getEmail());
        builder.params(PROXY, StringUtils.isNotEmpty(notificationRequestDto.getProxy()) ? notificationRequestDto.getProxy() : PROXY);
        builder.params(TIME, StringUtils.defaultIfBlank(notificationRequestDto.getTime(), DEFAULT_STR));
        if (notificationRequestDto.getDate() != null) {
            builder.params(DATE, StringUtils.defaultIfBlank(notificationRequestDto.getDate(), DEFAULT_STR));
        }
        builder.params(REFERENCE_NUMBER, StringUtils.defaultIfBlank(notificationRequestDto.getReferenceNumber(), DEFAULT_STR));
        builder.params(ALTERNATE_STEPS, emailConfig.getAlternateSteps());
        builder.params(ALTERNATE_STEPS_IF_ANY, emailConfig.getAlternateSteps());
        builder.params(REASON_FOR_FAILURE, StringUtils.defaultIfBlank(notificationRequestDto.getReasonForFailure(), DEFAULT_STR));

        builder.params(RECEIVER_NAME, StringUtils.defaultIfBlank(notificationRequestDto.getReceiverName(), CUSTOMER_DEFAULT));
        builder.params(FROM_ACCOUNT, StringUtils.defaultIfBlank(notificationRequestDto.getFromAccount(), DEFAULT_STR));
        builder.params(PAYMENT_NOTE, StringUtils.defaultIfBlank(notificationRequestDto.getReasonForFailure(), NOT_APPLICABLE));
        builder.params(EMAIL_PROXY, Optional.ofNullable(notificationRequestDto.getEmailProxy()).isPresent() ? notificationRequestDto.getEmailProxy() : "");
        if (CollectionUtils.isNotEmpty(notificationRequestDto.getRtpNotificationList())) {
            if (notificationRequestDto.getRtpNotificationList().size() > 1) {
                List<RecipientDTO> recipientDTOS = new ArrayList<>();

                notificationRequestDto.getRtpNotificationList().forEach(rtpNotification -> {
                    RecipientDTO recipientDTO = RecipientDTO.builder()
                            .requestedAmount(REQUESTED_AMOUNT)
                            .aed(AED)
                            .contactName(rtpNotification.getContactName())
                            .value(EmailUtil.formattedAmount(rtpNotification.getAmount()))
                            .proxy(StringUtils.defaultIfBlank(rtpNotification.getProxy(), DEFAULT_STR))
                            .receiverName(StringUtils.defaultIfBlank(rtpNotification.getContactName(), CUSTOMER_DEFAULT))
                            .build();
                    recipientDTOS.add(recipientDTO);
                });
                log.info("NpssNotificationService >> getNewTemplateValuesForNotificationBuilder >> builder formed {} for the notification type{}",builder,notificationRequestDto.getNotificationType());
                builder.params(RECIPIENTS, recipientDTOS);
            }else{
                builder.params(AED, AED);
                builder.params(PROXY,StringUtils.defaultIfBlank(notificationRequestDto.getRtpNotificationList().get(0).getProxy(), DEFAULT_STR));
                builder.params(VALUE, StringUtils.defaultIfBlank(EmailUtil.formattedAmount(notificationRequestDto.getRtpNotificationList().get(0).getAmount()),DEFAULT_STR));
                builder.params(REASON_FOR_FAILURE, StringUtils.defaultIfBlank(notificationRequestDto.getReasonForFailure(), DEFAULT_STR));
            }
        }
    }

    private String getTemplateName (String type){
    	if (NotificationType.CREATE_EMAIL_PROXY_NOTIF_EVENT.equalsIgnoreCase(type)) {
            return MT_NPSS_EMAIL_PROXY_UPDATE;
        } else if (NotificationType.CUSTOMER_ENROLMENT.equalsIgnoreCase(type)) {
            return MT_NPSS_ENROLLMENT;
        } else if (NotificationType.PAYMENT_SUCCESS.equalsIgnoreCase(type)) {
            return MT_NPSS_PAYMENT_SUCCESSFUL;
        } else if (NotificationType.PAYMENT_FAIL.equalsIgnoreCase(type)) {
            return MT_NPSS_PAYMENT_FAILURE;
        } else if (NotificationType.PAYMENT_REQUEST_SENT_MULTIPLE_RTP.equalsIgnoreCase(type)) {
            return MT_NPSS_REQUEST_SENT_MULTIPLE;
        } else if (NotificationType.PAYMENT_REQUEST_SENT_MULTIPLE_FAIL_RTP.equalsIgnoreCase(type)) {
            return MT_NPSS_REQUEST_SENT_MULTIPLE_FAIL;
        } else if (NotificationType.PAYMENT_REQUEST_SENT_RTP.equalsIgnoreCase(type)) {
            return MT_NPSS_REQUEST_SENT;
        } else if (NotificationType.PAYMENT_REQUEST_SENT_FAIL_RTP.equalsIgnoreCase(type)) {
            return MT_NPSS_REQUEST_SENT_FAILURE;
        }else if (NotificationType.P2P_BEN_RIC_DEN.equalsIgnoreCase(type) || NotificationType.P2P_BEN_RIC_DEN_CAUSALE.equalsIgnoreCase(type)) {
            return MT_NPSS_REQUEST_RECEIVED;
        } else if (NotificationType.P2P_ORD_RIC_DEN_ANN_M.equalsIgnoreCase(type)) {
            return MT_NPSS_REQUEST_SENT_DECLINED;
        } else if (NotificationType.P2P_ORD_RIC_DEN_ANN_A.equalsIgnoreCase(type)) {
            return MT_NPSS_REQUEST_SENT_EXPITED;
        } else if (NotificationType.MOBILE_PHONE_NUMBER_CHANGED.equalsIgnoreCase(type)) {
            return MT_NPSS_MOBILE_PHONE_NUMBER_CHANGED; 
        }else
            return NOT_APPLICABLE;
    }

    private com.mashreq.notification.client.freemarker.TemplateRequest.EmailBuilder buildEmailTemplate(String templateName,RequestMetaData metaData,String channelType) {
        log.info("NpssNotificationService >> buildEmailTemplate >> CIF {} and segment {}",metaData.getPrimaryCif(), metaData.getSegment());
        return  com.mashreq.notification.client.freemarker.TemplateRequest.emailBuilder()
                    .templateType(TemplateType.EMAIL)
                    .templateName(templateName)
                    .country(metaData.getCountry())
                    .segment(metaData.getSegment())
                    .channel(channelType)
                    .businessType(BUSINESS_TYPE)
                    .language(defaultLanguage)
                    .subjectParams(CHANNEL_TYPE, channelType)
                    .toEmailAddress(metaData.getEmail());
        }

    private TemplateRequest.SMSBuilder buildSMSTemplate(NotificationRequestDto notifReqDto,RequestMetaData metaData) {
        boolean isMobile = metaData.getChannel().contains(MOBILE);
        String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
        log.info("NpssNotificationService >> buildSMSTemplate >>CIF {} and  segment {}",metaData.getPrimaryCif(),metaData.getSegment());
        if(StringUtils.isEmpty(metaData.getSegment())){
            log.info("NpssNotificationService >> buildSMSTemplate >> set segment for CIF {}",metaData.getPrimaryCif() );
        	RequestMetaData requestMetaData = RequestMetaData.builder().primaryCif(metaData.getPrimaryCif()).build();
            DigitalUser digitalUser = digitalUserService.getDigitalUser(requestMetaData);
            metaData.setSegment(digitalUser.getDigitalUserGroup().getSegment().getName());
            log.info("NpssNotificationService >> buildSMSTemplate >> CIF {} and  segment {}",metaData.getPrimaryCif(), metaData.getSegment() );
        }
        
        return TemplateRequest.smsBuilder()
                .templateType(TemplateType.SMS)
                .templateName(getTemplateName(notifReqDto.getNotificationType()))
                .businessType(BUSINESS_TYPE)
                .channel(channelType)
                .country(metaData.getCountry())
                .language(defaultLanguage)
                .segment(metaData.getSegment())
                .params(EMAIL_PROXY, notifReqDto.getEmailProxy())
                /*.params(DATE_TIME, dateTime)*/
                .mobileNumber(metaData.getMobileNUmber());
    }

    private void sendSMSNotification(NotificationRequestDto notifReqDto,RequestMetaData metaData){
        log.info("NpssNotificationService >> sendSMSNotification start >> cif {}, request {}",metaData.getPrimaryCif(),notifReqDto);
        try {
            TemplateRequest.SMSBuilder templateRequest  = buildSMSTemplate(notifReqDto, metaData);
            buildSMSBody(templateRequest,notifReqDto);
            log.info("NpssNotificationService >> sendSMSNotification, templated created >> cif {}",metaData.getPrimaryCif());
            notificationService.sendNotification(templateRequest.configure());
            log.info("NpssNotificationService >> sendSMSNotification,SMS sent >> cif {}",metaData.getPrimaryCif());
         }catch (Exception exception){
            log.error("NpssNotificationService >> sendSMSNotification error occured >> cif {},exception {} ",metaData.getPrimaryCif(),exception.getMessage());
        }
    }

    private void buildSMSBody(TemplateRequest.SMSBuilder builder,NotificationRequestDto notificationRequestDto) {
        builder.params(CUSTOMER_NAME, StringUtils.defaultIfBlank(notificationRequestDto.getCustomerName(), CUSTOMER));
        builder.params(AMOUNT, null != notificationRequestDto.getAmount() ? EmailUtil.formattedAmount(notificationRequestDto.getAmount()) : BigDecimal.ZERO);
        builder.params(CURRENCY,AED);
        builder.params(REFERENCE_NUMBER,notificationRequestDto.getReferenceNumber());
        builder.params(BENEFICIARY_BANK_NAME,notificationRequestDto.getContactName());
        builder.params(TO_ACCOUNT_NO,notificationRequestDto.getSentTo());
        builder.params(CUSTOMER_NAME,notificationRequestDto.getCustomerName());
        builder.params(SENT_TO,builder.configure().getMobileNumber());
        builder.params(PROXY, StringUtils.isNotEmpty(notificationRequestDto.getProxy()) ? notificationRequestDto.getProxy() : PROXY);
        builder.params(RECEIVER_NAME, StringUtils.defaultIfBlank(notificationRequestDto.getContactName(), CUSTOMER_DEFAULT));
        builder.params(SENDER_NAME, StringUtils.defaultIfBlank(notificationRequestDto.getCustomerName(), CUSTOMER));
    }

}
