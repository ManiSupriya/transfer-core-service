package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.notification.model.EmailParameters;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateParameters;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.mashreq.transfercoreservice.notification.service.EmailUtil.*;


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

    /**
     * Send Alerts via sms, email and push notification.
     *
     * @param requestMetaData

     * @param
     */

    @Async("generalTaskExecutor")
    public void performPostTransactionActivities(RequestMetaData requestMetaData, FundTransferRequest fundTransferRequest){
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        try {
            final PostTransactionActivityContext<SendEmailRequest> emailPostTransactionActivityContext = getEmailPostTransactionActivityContext(requestMetaData, fundTransferRequest);
            postTransactionActivityService.execute(Arrays.asList(emailPostTransactionActivityContext));
            userEventPublisher.publishSuccessEvent(eventType, requestMetaData, eventType.getDescription());
        }catch (Exception exception){
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }

    }

    private PostTransactionActivityContext<SendEmailRequest> getEmailPostTransactionActivityContext(RequestMetaData requestMetaData,
                                                                                                    FundTransferRequest fundTransferRequest) throws Exception {
        SendEmailRequest emailRequest = SendEmailRequest.builder().isEmailPresent(false).build();
        if (StringUtils.isNotBlank(requestMetaData.getEmail())) {
            final EmailParameters emailParameters = emailConfig.getEmail().get(requestMetaData.getCountry());
            final EmailTemplateParameters emailTemplateParameters = emailUtil.getEmailTemplateParameters(requestMetaData.getChannel(), requestMetaData.getSegment());
            boolean isMobile =  requestMetaData.getChannel().contains(MOBILE) ? true: false;
            String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
            Map<String, String> templateValues = new HashMap<>();
            final String subject = emailParameters.getEmailSubject(fundTransferRequest.getNotificationType(),fundTransferRequest.getTransferType(),channelType);
            templateValues.put(TRANSFER_TYPE, StringUtils.defaultIfBlank(fundTransferRequest.getTransferType(), DEFAULT_STR));
            templateValues.put(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR));
            templateValues.put(CUSTOMER_NAME, StringUtils.defaultIfBlank(emailUtil.capitalizeFully(requestMetaData.getUsername()), CUSTOMER));
            templateValues.put(SOURCE_OF_FUND, SOURCE_OF_FUND_ACCOUNT);
            templateValues.put(BANK_NAME, StringUtils.defaultIfBlank(emailTemplateParameters.getChannelIdentifier().getChannelName(), DEFAULT_STR) );
            templateValues.put(CHANNEL_TYPE, StringUtils.defaultIfBlank(channelType, DEFAULT_STR));
            templateValues.put(CONTACT_HTML_BODY_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getHtmlContactContents().getHtmlContent(),DEFAULT_STR));
            templateValues.put(FACEBOOK_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(FACEBOOK), DEFAULT_STR));
            templateValues.put(INSTAGRAM_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(INSTAGRAM), DEFAULT_STR));
            templateValues.put(TWITTER_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(TWITTER), DEFAULT_STR));
            templateValues.put(LINKED_IN_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(LINKED_IN), DEFAULT_STR));
            templateValues.put(YOUTUBE_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(YOUTUBE), DEFAULT_STR));

            if(fundTransferRequest.getNotificationType().matches(NotificationType.GOLD_SILVER_BUY_SUCCESS)){
                getTemplateValuesForBuyGoldSilver(templateValues,fundTransferRequest);
            }
            else if(fundTransferRequest.getNotificationType().matches(NotificationType.GOLD_SILVER_SELL_SUCCESS)){
                getTemplateValuesForSellGoldSilver(templateValues,fundTransferRequest);
            }
            else {
                getTemplateValuesForFundTransfer(templateValues,fundTransferRequest);
            }


            emailRequest = SendEmailRequest.builder()
                    .fromEmailAddress(isMobile ?emailParameters.getFromEmailAddressMob():emailParameters.getFromEmailAddressWeb())
                    .toEmailAddress(requestMetaData.getEmail())
                    .subject(subject)
                    .fromEmailName( isMobile ?emailParameters.getFromEmailNameMob():emailParameters.getFromEmailNameWeb())
                    .templateName(emailParameters.getEmailTemplate(fundTransferRequest.getNotificationType()))
                    .templateKeyValues(templateValues)
                    .isEmailPresent(true)
                    .build();
        }
        return PostTransactionActivityContext.<SendEmailRequest>builder().payload(emailRequest).postTransactionActivity(sendEmailActivity).build();
    }

    private void getTemplateValuesForBuyGoldSilver(Map<String, String> templateValues, FundTransferRequest fundTransferRequest) throws Exception {
        templateValues.put(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
        templateValues.put(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getToAccount()), DEFAULT_STR));
        templateValues.put(TXN_AMOUNT, StringUtils.defaultIfBlank(String.valueOf(fundTransferRequest.getAmount()), DEFAULT_STR));
        templateValues.put(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getSourceCurrency(), DEFAULT_STR) );
        templateValues.put(AMOUNT, StringUtils.defaultIfBlank(String.valueOf(fundTransferRequest.getSrcAmount()), DEFAULT_STR));
        templateValues.put(STATUS, STATUS_SUCCESS);

    }

    private void getTemplateValuesForSellGoldSilver(Map<String, String> templateValues, FundTransferRequest fundTransferRequest) throws Exception {
        templateValues.put(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
        templateValues.put(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getToAccount()), DEFAULT_STR));
        templateValues.put(TXN_AMOUNT, StringUtils.defaultIfBlank(String.valueOf(fundTransferRequest.getSrcAmount()), DEFAULT_STR));
        templateValues.put(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getDestinationCurrency(), DEFAULT_STR) );
        templateValues.put(AMOUNT, StringUtils.defaultIfBlank(String.valueOf(fundTransferRequest.getAmount()), DEFAULT_STR));
        templateValues.put(STATUS, STATUS_SUCCESS);
    }

    private void getTemplateValuesForFundTransfer(Map<String, String> templateValues, FundTransferRequest fundTransferRequest) throws Exception {
        templateValues.put(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
        templateValues.put(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(fundTransferRequest.getToAccount(), DEFAULT_STR));
        templateValues.put(BENEFICIARY_NICK_NAME, StringUtils.defaultIfBlank(fundTransferRequest.getBeneficiaryFullName(), DEFAULT_STR));
        templateValues.put(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getSourceCurrency(), DEFAULT_STR) );
        templateValues.put(AMOUNT, StringUtils.defaultIfBlank(String.valueOf(fundTransferRequest.getAmount()), DEFAULT_STR));
        templateValues.put(STATUS, StringUtils.defaultIfBlank(fundTransferRequest.getStatus(), DEFAULT_STR));
    }
}
