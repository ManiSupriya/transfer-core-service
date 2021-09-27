package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.templates.freemarker.TemplateEngine;
import com.mashreq.templates.freemarker.TemplateRequest;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.EmailParameters;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateParameters;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import com.mashreq.transfercoreservice.paylater.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static com.mashreq.transfercoreservice.notification.service.EmailUtil.*;
import static java.lang.Long.valueOf;


@Service
@Slf4j
public class PostTransactionService {

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private PostTransactionActivityService postTransactionActivityService;

    @Autowired
    private SendEmailActivity sendEmailActivity;

    @Autowired
    private AsyncUserEventPublisher userEventPublisher;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private BeneficiaryService beneficiaryService;

    /**
     * Send Alerts via sms, email and push notification.
     *
     * @param requestMetaData

     * @param
     */

    @Async("generalTaskExecutor")
    public void performPostTransactionActivities(RequestMetaData requestMetaData, FundTransferRequest fundTransferRequest, FundTransferRequestDTO fundTransferRequestDTO){
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        try {
            final PostTransactionActivityContext<SendEmailRequest> emailPostTransactionActivityContext = getEmailPostTransactionActivityContext(requestMetaData, fundTransferRequest, fundTransferRequestDTO);
            postTransactionActivityService.execute(Arrays.asList(emailPostTransactionActivityContext), requestMetaData);
            userEventPublisher.publishSuccessEvent(eventType, requestMetaData, eventType.getDescription());
        }catch (Exception exception){
            GenericExceptionHandler.logOnly(exception, transferErrorCode.getErrorMessage());
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }

    }


    private PostTransactionActivityContext<SendEmailRequest> getEmailPostTransactionActivityContext(RequestMetaData requestMetaData,
                                                                                                    FundTransferRequest fundTransferRequest,
                                                                                                    FundTransferRequestDTO fundTransferRequestDTO) throws Exception {


        SendEmailRequest emailRequest = SendEmailRequest.builder().isEmailPresent(false).build();
        String contactLinkText;
        String htmlContent;

        if (StringUtils.isNotBlank(requestMetaData.getEmail())) {
            final EmailParameters emailParameters = emailConfig.getEmail().get(requestMetaData.getCountry());

            final String templateName = emailParameters.getEmailTemplate(fundTransferRequest.getNotificationType());
            final EmailTemplateParameters emailTemplateParameters = emailUtil.getEmailTemplateParameters(requestMetaData.getChannel(), requestMetaData.getSegment());
            boolean isMobile = requestMetaData.getChannel().contains(MOBILE);
            String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
            Segment segment = emailTemplateParameters.getSegment();
            final String subject = emailParameters.getEmailSubject(fundTransferRequest.getNotificationType(),fundTransferRequest.getTransferType(),channelType);

            String contactHtmlBody;
            String segmentSignOffCompanyName;

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
            } else {
                contactHtmlBody = DEFAULT_STR;
                segmentSignOffCompanyName = DEFAULT_STR;
            }

            TemplateRequest.Builder template = TemplateRequest.builder()
                    .templateName(templateName)
                    .params(TRANSFER_TYPE, StringUtils.defaultIfBlank(fundTransferRequest.getTransferType(), DEFAULT_STR))
                    .params(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR))
                    .params(CUSTOMER_NAME, StringUtils.defaultIfBlank(emailUtil.capitalizeFully(requestMetaData.getUsername()), CUSTOMER))
                    .params(SOURCE_OF_FUND, fundTransferRequest.getSourceOfFund() == null ? SOURCE_OF_FUND_ACCOUNT: fundTransferRequest.getSourceOfFund())
                    .params(BANK_NAME, StringUtils.defaultIfBlank(emailTemplateParameters.getChannelIdentifier().getChannelName(), DEFAULT_STR))
                    .params(CHANNEL_TYPE, StringUtils.defaultIfBlank(channelType, DEFAULT_STR))
                    .params(FACEBOOK_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(FACEBOOK), DEFAULT_STR))
                    .params(INSTAGRAM_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(INSTAGRAM), DEFAULT_STR))
                    .params(TWITTER_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(TWITTER), DEFAULT_STR))
                    .params(LINKED_IN_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(LINKED_IN), DEFAULT_STR))
                    .params(YOUTUBE_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(YOUTUBE), DEFAULT_STR))
                    .params(EMAIL_TEMPLATE_COPYRIGHT_YEAR_KEY, String.valueOf(LocalDateTime.now().getYear()))
                    .params(CONTACT_HTML_BODY_KEY, contactHtmlBody)
                    .params(SEGMENT_SIGN_OFF_COMPANY_NAME, segmentSignOffCompanyName);

            if(fundTransferRequest.getNotificationType().matches(NotificationType.GOLD_SILVER_BUY_SUCCESS)){
                getTemplateValuesForBuyGoldSilverBuilder(template, fundTransferRequest);
            }
            else if(fundTransferRequest.getNotificationType().matches(NotificationType.GOLD_SILVER_SELL_SUCCESS)){
                getTemplateValuesForSellGoldSilverBuilder(template, fundTransferRequest);
            }
            else {
                getTemplateValuesForFundTransferBuilder(template, fundTransferRequest, fundTransferRequestDTO, requestMetaData, segment);
            }

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
    private void getTemplateValuesForBuyGoldSilverBuilder(TemplateRequest.Builder builder, FundTransferRequest fundTransferRequest) throws Exception {
        builder.params(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
        builder.params(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getToAccount()), DEFAULT_STR));
        builder.params(TXN_AMOUNT, fundTransferRequest.getAmount()!=null?EmailUtil.formattedAmount(fundTransferRequest.getAmount()) : DEFAULT_STR);
        builder.params(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getSourceCurrency(), DEFAULT_STR) );
        builder.params(AMOUNT, fundTransferRequest.getSrcAmount()!=null? EmailUtil.formattedAmount(fundTransferRequest.getSrcAmount()): DEFAULT_STR);
        builder.params(STATUS, STATUS_SUCCESS);

    }
    private void getTemplateValuesForSellGoldSilverBuilder(TemplateRequest.Builder builder, FundTransferRequest fundTransferRequest) throws Exception {
        builder.params(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
        builder.params(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getToAccount()), DEFAULT_STR));
        builder.params(TXN_AMOUNT, fundTransferRequest.getSrcAmount()!=null? EmailUtil.formattedAmount(fundTransferRequest.getSrcAmount()) : DEFAULT_STR);
        builder.params(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getDestinationCurrency(), DEFAULT_STR) );
        builder.params(AMOUNT, fundTransferRequest.getAmount() !=null? EmailUtil.formattedAmount(fundTransferRequest.getAmount()) : DEFAULT_STR);
        builder.params(STATUS, STATUS_SUCCESS);
    }
    private void getTemplateValuesForFundTransferBuilder(TemplateRequest.Builder builder, FundTransferRequest fundTransferRequest,
                                                         FundTransferRequestDTO fundTransferRequestDTO, RequestMetaData requestMetaData, Segment segment) {
        builder.params(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
        builder.params(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getToAccount()), DEFAULT_STR));
        builder.params(BENEFICIARY_NICK_NAME, StringUtils.defaultIfBlank(fundTransferRequest.getBeneficiaryFullName(), DEFAULT_STR));
        builder.params(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getTxnCurrency(), DEFAULT_STR) );
        if(fundTransferRequest.getAmount() != null) {
            builder.params(AMOUNT, EmailUtil.formattedAmount(fundTransferRequest.getAmount()));
        }
        else if(fundTransferRequest.getSrcAmount() != null){
            builder.params(AMOUNT, EmailUtil.formattedAmount(fundTransferRequest.getSrcAmount()));
        }
        else {
            builder.params(AMOUNT, DEFAULT_STR);
        }
        builder.params(STATUS, STATUS_SUCCESS);

        if(fundTransferRequest.getNotificationType().contains("PL") || fundTransferRequest.getNotificationType().contains("SI")){
            final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(requestMetaData.getPrimaryCif(), valueOf(fundTransferRequestDTO.getBeneficiaryId()), fundTransferRequestDTO.getJourneyVersion(), requestMetaData);

            builder.params(BENEFICIARY_BANK_NAME, StringUtils.defaultIfBlank(beneficiaryDto.getBankName(), DEFAULT_STR));
            builder.params(BENEFICIARY_BANK_COUNTRY, StringUtils.defaultIfBlank(beneficiaryDto.getBankCountry(), DEFAULT_STR));
            builder.params(CUSTOMER_CARE_NO, StringUtils.defaultIfBlank(segment.getCustomerCareNumber(), DEFAULT_STR));
            builder.params(TRANSACTION_DATE, StringUtils.defaultIfBlank(
                    DateUtil.instantToDate(Instant.now(), "dd-MM-yyyy HH:mm:ss"), DEFAULT_STR)
            );
            builder.params(TRANSACTION_TYPE, StringUtils.defaultIfBlank(
                    fundTransferRequestDTO.getOrderType().equals("PL") ? "Pay Later" : "Standing Instructions", DEFAULT_STR)
            );
        }
    }

    // Below code is used for testing
    /*private FundTransferRequest buildTestData(){
        FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .sourceOfFund(SOURCE_OF_FUND_ACCOUNT)
                .amount(new BigDecimal("10.11"))
                .transferType("Own Account")
                .NotificationType(NotificationType.OTHER_ACCOUNT_TRANSACTION)
                .fromAccount("019100341109")
                .toAccount("019100341109")
                .beneficiaryFullName("XYZ")
                .txnCurrency("AED")
                .build();

        return fundTransferRequest;
    }*/
}
