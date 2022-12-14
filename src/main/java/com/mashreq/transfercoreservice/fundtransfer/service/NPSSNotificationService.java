package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.templates.freemarker.TemplateEngine;
import com.mashreq.templates.freemarker.TemplateRequest;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.EmailParameters;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateParameters;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import com.mashreq.transfercoreservice.notification.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.LOCAL_FT;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;
import static com.mashreq.transfercoreservice.notification.service.EmailUtil.*;

/**
 * Created by KrishnaKo on 24/11/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NPSSNotificationService {
    private final NotificationService notificationService;
    private final EmailConfig emailConfig;
    private final PostTransactionActivityService postTransactionActivityService;

    private final SendEmailActivity sendEmailActivity;

    private final AsyncUserEventPublisher userEventPublisher;

    private final EmailUtil emailUtil;
    private final TemplateEngine templateEngine;

    @Async("generalTaskExecutor")
    public void performPostTransactionActivities(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto){
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        try {
            final PostTransactionActivityContext<SendEmailRequest> emailPostTransactionActivityContext = getEmailPostTransactionActivityContext(requestMetaData, notificationRequestDto);
            postTransactionActivityService.execute(Arrays.asList(emailPostTransactionActivityContext), requestMetaData);
            userEventPublisher.publishSuccessEvent(eventType, requestMetaData, eventType.getDescription());
        }catch (Exception exception){
            GenericExceptionHandler.logOnly(exception, transferErrorCode.getErrorMessage());
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }

    }
    private PostTransactionActivityContext<SendEmailRequest> getEmailPostTransactionActivityContext(RequestMetaData requestMetaData,
                                                                                                    NotificationRequestDto notificationRequestDto ) throws Exception {


        SendEmailRequest emailRequest = SendEmailRequest.builder().isEmailPresent(false).build();
        String contactLinkText;
        String htmlContent;

        if (StringUtils.isNotBlank(requestMetaData.getEmail())) {
            final EmailParameters emailParameters = emailConfig.getEmail().get(requestMetaData.getCountry());

            final String templateName = emailParameters.getEmailTemplate(notificationRequestDto.getNotificationType());
            final EmailTemplateParameters emailTemplateParameters = emailUtil.getEmailTemplateParameters(requestMetaData.getChannel(), requestMetaData.getSegment());
            boolean isMobile = requestMetaData.getChannel().contains(MOBILE);
            String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
            Segment segment = emailTemplateParameters.getSegment();
            final String subject = emailParameters.getEmailSubject(notificationRequestDto.getNotificationType(),notificationRequestDto.getTransferType(),channelType);

            String contactHtmlBody;
            String segmentSignOffCompanyName;
            String bankNameInFooter;
            String bankNameInFooterDesc;
            if(segment != null) {
                contactLinkText = StringUtils.defaultIfBlank(segment.getEmailContactUsLinkText(), DEFAULT_STR);
                htmlContent = segment.getEmailContactUsHtmlContent();
                if(StringUtils.isNotEmpty(htmlContent)) {
                    htmlContent = htmlContent.replaceAll("\\{contactUsLinkText}", contactLinkText);
                    htmlContent = htmlContent.replaceAll("\\$", DEFAULT_STR);
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

            TemplateRequest.Builder template = TemplateRequest.builder()
                    .templateName(templateName)
                    .params(TRANSFER_TYPE, StringUtils.defaultIfBlank(notificationRequestDto.getTransferType(), DEFAULT_STR))
                    .params(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR))
                    .params(CUSTOMER_NAME, StringUtils.defaultIfBlank(emailUtil.capitalizeFully(requestMetaData.getUsername()), CUSTOMER))
                    .params(SOURCE_OF_FUND, notificationRequestDto.getSourceOfFund() == null ? SOURCE_OF_FUND_ACCOUNT: notificationRequestDto.getSourceOfFund())
                    .params(BANK_NAME, StringUtils.defaultIfBlank(emailTemplateParameters.getChannelIdentifier().getChannelName(), DEFAULT_STR))
                    .params(CHANNEL_TYPE, StringUtils.defaultIfBlank(channelType, DEFAULT_STR))
                    .params(FACEBOOK_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(FACEBOOK), DEFAULT_STR))
                    .params(INSTAGRAM_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(INSTAGRAM), DEFAULT_STR))
                    .params(TWITTER_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(TWITTER), DEFAULT_STR))
                    .params(LINKED_IN_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(LINKED_IN), DEFAULT_STR))
                    .params(YOUTUBE_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(YOUTUBE), DEFAULT_STR))
                    .params(EMAIL_TEMPLATE_COPYRIGHT_YEAR_KEY, String.valueOf(LocalDateTime.now().getYear()))
                    .params(CONTACT_HTML_BODY_KEY, contactHtmlBody)
                    .params(SEGMENT_SIGN_OFF_COMPANY_NAME, segmentSignOffCompanyName)
                    .params(BANK_NAME_FOOTER, bankNameInFooter)
                    .params(BANK_NAME_FOOTER_DESC, bankNameInFooterDesc);


            getTemplateValuesForNotificationBuilder(template, notificationRequestDto, requestMetaData, segment);

            emailRequest = SendEmailRequest.builder()
                    .fromEmailAddress(emailParameters.getFromEmailAddress())
                    .toEmailAddress(requestMetaData.getEmail())
                    .subject(subject)
                    .text(templateEngine.generate(template.configure()))
                    .fromEmailName(emailParameters.getFromEmailName())
                    .isEmailPresent(true)
                    .build();
        }
        return PostTransactionActivityContext.<SendEmailRequest>builder().payload(emailRequest).postTransactionActivity(sendEmailActivity).build();
    }
    private void getTemplateValuesForNotificationBuilder(TemplateRequest.Builder builder, NotificationRequestDto notificationRequestDto,
                                                          RequestMetaData requestMetaData, Segment segment) {
        builder.params(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(notificationRequestDto.getFromAccount()), DEFAULT_STR));
        builder.params(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(emailUtil.doMask(notificationRequestDto.getToAccount()), DEFAULT_STR));
        builder.params(BENEFICIARY_NICK_NAME, StringUtils.defaultIfBlank(notificationRequestDto.getBeneficiaryFullName(), DEFAULT_STR));
        builder.params(CURRENCY, StringUtils.defaultIfBlank(notificationRequestDto.getTxnCurrency(), DEFAULT_STR) );
        builder.params(DESTINATION_ACCOUNT_CURRENCY,StringUtils.defaultIfBlank(notificationRequestDto.getDestinationCurrency(), DEFAULT_STR));
        builder.params(ACCOUNT_CURRENCY,StringUtils.defaultIfBlank(notificationRequestDto.getSourceCurrency(), DEFAULT_STR));
        builder.params(SOURCE_AMOUNT,notificationRequestDto.getSrcCcyAmt() != null ? EmailUtil.formattedAmount(notificationRequestDto.getSrcCcyAmt()) : DEFAULT_STR);
        builder.params(BANK_FEES,StringUtils.defaultIfBlank(notificationRequestDto.getBankFees(), DEFAULT_STR));
        builder.params(FX_DEAL_CODE,StringUtils.defaultIfBlank(notificationRequestDto.getDealNumber(), DEFAULT_STR));
     //   builder.params(ORDER_TYPE,StringUtils.defaultIfBlank(notificationRequestDto.getOrderType(), DEFAULT_STR));
        builder.params(EXCHANGE_RATE,StringUtils.defaultIfBlank(notificationRequestDto.getExchangeRateDisplayTxt(), DEFAULT_STR));
        builder.params(LOCAL_CURRENCY,AED);

        if(notificationRequestDto.getAmount() != null) {
            builder.params(AMOUNT, EmailUtil.formattedAmount(notificationRequestDto.getAmount()));
        }
        else if(notificationRequestDto.getSrcAmount() != null){
            builder.params(AMOUNT, EmailUtil.formattedAmount(notificationRequestDto.getSrcAmount()));
        }
        else {
            builder.params(AMOUNT, DEFAULT_STR);
        }
        builder.params(STATUS, STATUS_SUCCESS);

/*        if((notificationRequestDto.getNotificationType().contains("PL") || notificationRequestDto.getNotificationType().contains("SI"))){

            ServiceType serviceType = getServiceByType(notificationRequestDto.getServiceType());
          *//*  if(OWN_ACCOUNT_SERVICE_TYPES.contains(serviceType)){
                builder.params(BENEFICIARY_BANK_NAME, StringUtils.defaultIfBlank(segment.getEmailCprFooter(), DEFAULT_STR));
                builder.params(BENEFICIARY_BANK_COUNTRY, StringUtils.defaultIfBlank(address, DEFAULT_STR));
            }
            else{
                final BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(requestMetaData.getPrimaryCif(), valueOf(fundTransferRequestDTO.getBeneficiaryId()), fundTransferRequestDTO.getJourneyVersion(), requestMetaData);
                builder.params(BENEFICIARY_BANK_NAME, StringUtils.defaultIfBlank(beneficiaryDto.getBankName(), DEFAULT_STR));
                builder.params(BENEFICIARY_BANK_COUNTRY, StringUtils.defaultIfBlank(beneficiaryDto.getBankCountry(), DEFAULT_STR));
            }*//*

            builder.params(CUSTOMER_CARE_NO, StringUtils.defaultIfBlank(segment.getCustomerCareNumber(), DEFAULT_STR));
            builder.params(TRANSACTION_DATE, StringUtils.defaultIfBlank(
                    DateUtil.instantToDate(Instant.now(), "yyyy-MM-dd HH:mm:ss"), DEFAULT_STR)
            );
            builder.params(TRANSACTION_TYPE, StringUtils.defaultIfBlank(
                    fundTransferRequestDTO.getOrderType().equals("PL") ? "Pay Later" : "Standing Instructions", DEFAULT_STR)
            );

            builder.params(EXECUTION_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getStartDate(), DEFAULT_STR));
            builder.params(START_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getStartDate(), DEFAULT_STR));
            builder.params(END_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getEndDate(), DEFAULT_STR));
            builder.params(FREQUENCY,StringUtils.defaultIfBlank(fundTransferRequestDTO.getFrequency(), DEFAULT_STR));
        }*/
    }

    private CustomerNotification populateCustomerNotification(String transactionRefNo, String currency, BigDecimal amount, String beneficiaryName, String creditAccount) {
        CustomerNotification customerNotification = new CustomerNotification();
        customerNotification.setAmount(String.valueOf(amount));
        customerNotification.setCurrency(currency);
        customerNotification.setTxnRef(transactionRefNo);
        customerNotification.setBeneficiaryName(beneficiaryName);
        customerNotification.setCreditAccount(creditAccount);
        return customerNotification;
    }

    public void performNotificationActivities(RequestMetaData requestMetaData, NotificationRequestDto notificationRequestDto ,UserDTO userDTO) {
        FundTransferRequest fundTransferRequest = null;
        final CustomerNotification customerNotification = populateCustomerNotification(notificationRequestDto.getTransactionReferenceNo(), fundTransferRequest.getTxnCurrency(),
                fundTransferRequest.getAmount(), fundTransferRequest.getBeneficiaryFullName(), fundTransferRequest.getToAccount());
        notificationService.sendNotifications(customerNotification, LOCAL_FT, requestMetaData, userDTO);

        notificationRequestDto.setTransferType("NPSS");
        notificationRequestDto.setNotificationType(OTHER_ACCOUNT_TRANSACTION);
        notificationRequestDto.setStatus(notificationRequestDto.getResponseStatus());

        performPostTransactionActivities(requestMetaData, notificationRequestDto);
    }

}
