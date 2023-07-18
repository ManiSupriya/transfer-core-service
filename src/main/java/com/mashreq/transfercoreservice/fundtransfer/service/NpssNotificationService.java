package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.templates.freemarker.TemplateEngine;
import com.mashreq.templates.freemarker.TemplateRequest;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.dto.RecipientDTO;
import com.mashreq.transfercoreservice.dto.RtpNotification;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.EmailParameters;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateParameters;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import com.mashreq.transfercoreservice.notification.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final PostTransactionActivityService postTransactionActivityService;

    private final SendEmailActivity sendEmailActivity;

    private final AsyncUserEventPublisher userEventPublisher;

    private final EmailUtil emailUtil;
    private TemplateEngine templateEngine;

    @Async("generalTaskExecutor")
    public void performPostTransactionActivities(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto) {
        log.info("NpssNotificationService >> performPostTransactionActivities >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        try {
            log.info("NpssNotificationService >> getEmailPostTransactionActivityContext Initiated >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            final PostTransactionActivityContext<SendEmailRequest> emailPostTransactionActivityContext = getEmailPostTransactionActivityContext(requestMetaData, notificationRequestDto);
            log.info("NpssNotificationService >> getEmailPostTransactionActivityContext Completed >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            postTransactionActivityService.execute(Collections.singletonList(emailPostTransactionActivityContext), requestMetaData);
            log.info("NpssNotificationService >> publishSuccessEvent Completed >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            userEventPublisher.publishSuccessEvent(eventType, requestMetaData, eventType.getDescription());
        } catch (Exception exception) {
            log.error("NpssNotificationService >> publishFailureEvent Completed >> for the cif {}{}",requestMetaData.getPrimaryCif(),notificationRequestDto);
            GenericExceptionHandler.logOnly(exception, transferErrorCode.getErrorMessage());
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }

    }

    private PostTransactionActivityContext<SendEmailRequest> getEmailPostTransactionActivityContext(RequestMetaData requestMetaData,
                                                                                                    NotificationRequestDto notificationRequestDto) throws Exception {
        log.info("NpssNotificationSuccess >> getEmailPostTransactionActivityContext >> {}",notificationRequestDto);
        SendEmailRequest emailRequest = SendEmailRequest.builder().isEmailPresent(false).build();
        log.info("NpssNotificationSuccess >> getEmailPostTransactionActivityContext >> emailRequest >> formed {}",emailRequest);
        String contactLinkText;
        String htmlContent;

        if (StringUtils.isNotBlank(requestMetaData.getEmail())) {
            final EmailParameters emailParameters = emailConfig.getEmail().get(requestMetaData.getCountry());

            final String templateName = emailParameters.getNpssEmailTemplate(notificationRequestDto.getNotificationType());
            log.info("NpssNotificationSuccess >> getEmailPostTransactionActivityContext >> templateName " +
                    "{} formed for the notificationType{}",templateName,notificationRequestDto.getNotificationType());
            final EmailTemplateParameters emailTemplateParameters = emailUtil.getEmailTemplateParameters(requestMetaData.getChannel(), requestMetaData.getSegment());
            boolean isMobile = requestMetaData.getChannel().contains(MOBILE);
            String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
            Segment segment = emailTemplateParameters.getSegment();
            final String subject = emailParameters.getNpssEmailSubject(notificationRequestDto.getNotificationType(), "NPSS", channelType);

            String contactHtmlBody;
            String segmentSignOffCompanyName;
            String bankNameInFooter;
            String bankNameInFooterDesc;
            if (segment != null) {
                contactLinkText = StringUtils.defaultIfBlank(segment.getEmailContactUsLinkText(), DEFAULT_STR);
                htmlContent = segment.getEmailContactUsHtmlContent();
                if (StringUtils.isNotEmpty(htmlContent)) {
                    htmlContent = htmlContent.replace("\\{contactUsLinkText}", contactLinkText);
                    htmlContent = htmlContent.replace("\\$", DEFAULT_STR);
                } else {
                    htmlContent = DEFAULT_STR;
                }

                contactHtmlBody = htmlContent;
                segmentSignOffCompanyName = StringUtils.defaultIfBlank(segment.getEmailSignOffCompany(), DEFAULT_STR);
                bankNameInFooter = StringUtils.defaultIfBlank(segment.getEmailCprFooter(), DEFAULT_STR);
                bankNameInFooterDesc = StringUtils.defaultIfBlank(segment.getEmailCprBankDesc(), DEFAULT_STR);
            } else {
                contactHtmlBody = DEFAULT_STR;
                segmentSignOffCompanyName = DEFAULT_STR;
                bankNameInFooter = emailTemplateParameters.getChannelIdentifier().getChannelName();
                bankNameInFooterDesc = DEFAULT_STR;
            }
            TemplateRequest.Builder template = null;
            try {
                template = TemplateRequest.builder()
                        .templateName(templateName)
                        .params(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR))
                        .params(CUSTOMER_NAME, Optional.ofNullable(notificationRequestDto.getCustomerName()).isPresent()
                                ? StringUtils.defaultIfBlank(emailUtil.capitalizeFully(notificationRequestDto.getCustomerName()), CUSTOMER)
                                : StringUtils.defaultIfBlank(emailUtil.capitalizeFully(requestMetaData.getUsername()), CUSTOMER))
                        .params(FACEBOOK_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(FACEBOOK), DEFAULT_STR))
                        .params(INSTAGRAM_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(INSTAGRAM), DEFAULT_STR))
                        .params(TWITTER_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(TWITTER), DEFAULT_STR))
                        .params(LINKED_IN_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(LINKED_IN), DEFAULT_STR))
                        .params(YOUTUBE_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(YOUTUBE), DEFAULT_STR))
                        .params(CONTACT_HTML_BODY_KEY, contactHtmlBody)
                        .params(SEGMENT_SIGN_OFF_COMPANY_NAME, segmentSignOffCompanyName)
                        .params(BANK_NAME_FOOTER, bankNameInFooter)
                        .params(BANK_NAME_FOOTER_DESC, bankNameInFooterDesc);
            } catch(Exception e){
            log.error("NpssNotificationSuccess >> getEmailPostTransactionActivityContext >> error in preparing template " +
                    "{} formed for the notificationType{}{}",templateName,notificationRequestDto.getNotificationType(),templateName);
            }
            getTemplateValuesForNotificationBuilder(template, notificationRequestDto);
            log.info("NpssNotificationSuccess >> getEmailPostTransactionActivityContext >> template preparation is successful " +
                    "{} formed for the notificationType{}{}{}",
                    templateName,notificationRequestDto.getNotificationType(),templateName,template);
            emailRequest = SendEmailRequest.builder()
                    .fromEmailAddress(emailParameters.getFromEmailAddress())
                    .toEmailAddress(requestMetaData.getEmail())
                    .subject(subject)
                    .text(templateEngine.generate(template.configure()))
                    .fromEmailName(emailParameters.getFromEmailName())
                    .isEmailPresent(true)
                    .build();
           log.info("NpssNotificationSuccess >> getEmailPostTransactionActivityContext >> final emailRequest formed {}{}",emailRequest,sendEmailActivity);
        }
        return PostTransactionActivityContext.<SendEmailRequest>builder().payload(emailRequest).postTransactionActivity(sendEmailActivity).build();
    }

    private void getTemplateValuesForNotificationBuilder(TemplateRequest.Builder builder, NotificationRequestDto notificationRequestDto) {

        if (notificationRequestDto.getAmount() != null) {
            builder.params(AMOUNT, EmailUtil.formattedAmount(notificationRequestDto.getAmount()));
        }
        if (notificationRequestDto.getSentTo() != null) {
            builder.params(SENT_TO, StringUtils.defaultIfBlank(notificationRequestDto.getSentTo(), DEFAULT_STR));
        }

        builder.params(TIME, StringUtils.defaultIfBlank(notificationRequestDto.getTime(), DEFAULT_STR));

        if (notificationRequestDto.getDate() != null) {
            builder.params(DATE, StringUtils.defaultIfBlank(notificationRequestDto.getDate(), DEFAULT_STR));
        }
        builder.params(REFERENCE_NUMBER, StringUtils.defaultIfBlank(notificationRequestDto.getReferenceNumber(), DEFAULT_STR));
        builder.params(ALTERNATE_STEPS, emailConfig.getAlternateSteps());
        builder.params(ALTERNATE_STEPS_IF_ANY, emailConfig.getAlternateSteps());
        builder.params(REASON_FOR_FAILURE, StringUtils.defaultIfBlank(notificationRequestDto.getReasonForFailure(), DEFAULT_STR));

        builder.params(CONTACT_NAME, StringUtils.defaultIfBlank(notificationRequestDto.getContactName(), CUSTOMER_DEFAULT));
        builder.params(FROM_ACCOUNT, StringUtils.defaultIfBlank(notificationRequestDto.getFromAccount(), DEFAULT_STR));

        builder.params(MESSAGE, StringUtils.defaultIfBlank(notificationRequestDto.getReasonForFailure(), DEFAULT_STR));
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
                            .build();
                    recipientDTOS.add(recipientDTO);
                });
                log.info("recipientDTOS formed {} for the notification type{}",recipientDTOS,notificationRequestDto.getNotificationType());
                builder.params(RECIPIENTS, recipientDTOS);
            }else{
                builder.params(AED, AED);
                builder.params(PROXY,StringUtils.defaultIfBlank(notificationRequestDto.getRtpNotificationList().get(0).getProxy(), DEFAULT_STR));
                builder.params(VALUE, StringUtils.defaultIfBlank(EmailUtil.formattedAmount(notificationRequestDto.getRtpNotificationList().get(0).getAmount()),DEFAULT_STR));
                builder.params(REASON_FOR_FAILURE, StringUtils.defaultIfBlank(notificationRequestDto.getReasonForFailure(), DEFAULT_STR));
            }
        }

    }

    private CustomerNotification populateCustomerNotification(NotificationRequestDto notificationRequestDto) {
        log.info("NpssNotificationService >> populateCustomerNotification >> {}",notificationRequestDto.toString());
        CustomerNotification customerNotification = new CustomerNotification();
        if (CUSTOMER_ENROLMENT.equalsIgnoreCase(notificationRequestDto.getNotificationType())||
                PAYMENT_REQUEST_SENT_MULTIPLE_RTP.equalsIgnoreCase(notificationRequestDto.getNotificationType())) {
            customerNotification.setCustomerName(notificationRequestDto.getCustomerName());
        } else {
            customerNotification.setAmount(String.valueOf(notificationRequestDto.getAmount()));
            customerNotification.setCurrency(AED);
            customerNotification.setTxnRef(notificationRequestDto.getReferenceNumber());
            customerNotification.setBeneficiaryName(notificationRequestDto.getContactName());
            customerNotification.setCreditAccount(notificationRequestDto.getSentTo());
            customerNotification.setCustomerName(notificationRequestDto.getCustomerName());
        }log.info("NpssNotificationService >> populateCustomerNotification >> customerNotification {}",customerNotification);

        return customerNotification;
    }

    public void performNotificationActivities(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto ,UserDTO userDTO) {
        if(PAYMENT_REQUEST_SENT.equalsIgnoreCase(notificationRequestDto.getNotificationType())){
            log.info("NpssEnrollmentService >> performNotificationActivities >> for Cif {} {}",requestMetaData.getPrimaryCif(),PAYMENT_REQUEST_SENT);
           List<RtpNotification> notificationRequestDtoList = notificationRequestDto.getRtpNotificationList().stream().map(this::mapRtpToNotificationRequest).collect(Collectors.toList());
           notificationRequestDto.setRtpNotificationList(notificationRequestDtoList);
            performSendNotifications(requestMetaData,notificationRequestDto,userDTO);
        }else{
            log.info("NpssEnrollmentService >> performNotificationActivities >> for Cif for non payment request sent requests{}",requestMetaData.getPrimaryCif());
            performSendNotifications(requestMetaData,  notificationRequestDto , userDTO);
        }

    }

    private RtpNotification mapRtpToNotificationRequest(RtpNotification notificationRequestDto) {
        return RtpNotification.builder().amount(notificationRequestDto.getAmount()).contactName(notificationRequestDto.getContactName()).sentTo(notificationRequestDto.getSentTo()).build();

    }

    private void performSendNotifications(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto ,UserDTO userDTO){
    log.info("NpssNotificationService >> performSendNotifications >> {} {} {}",requestMetaData.getPrimaryCif()
            ,notificationRequestDto.toString(),userDTO.toString());
    final CustomerNotification customerNotification = populateCustomerNotification(notificationRequestDto);
    notificationService.sendNotifications(customerNotification, notificationRequestDto.getNotificationType(), requestMetaData, userDTO);
    log.info("NpssNotificationService >> performPostTransactionActivities >> Initiated {}",notificationRequestDto);
    performPostTransactionActivities(requestMetaData, notificationRequestDto);
    log.info("NpssNotificationService >> performPostTransactionActivities >> Completed {}",notificationRequestDto);
}
}
