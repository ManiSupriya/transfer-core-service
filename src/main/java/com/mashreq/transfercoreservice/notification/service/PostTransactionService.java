package com.mashreq.transfercoreservice.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.model.*;
import com.mashreq.transfercoreservice.notification.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;



@Service
@Slf4j
public class PostTransactionService {

    private static final String APPLICATION_SETTINGS_CHANNEL_LOOKUP = "EMAIL_TEMPLATE_CHANNEL_LOOKUP";
    private static final String APPLICATION_SETTINGS_SOCIAL_MEDIA_LINKS = "EMAIL_TEMPLATE_SOCIAL_MEDIA_LINKS";
    private static final String APPLICATION_SETTINGS_CONTACT_US_HTML = "EMAIL_TEMPLATE_CONTACT_US_HTML_CONTENT";
    private static final String APPLICATION_SETTINGS_GROUP = "EMAIL_SETTINGS";
    private static final String LOCAL_EMAIL_TEMPLATE = ServiceType.LOCAL.getName();
    public static final String SOURCE_OF_FUND_ACCOUNT = "Account";

    private static final String MOBILE = "MOBILE";
    private static final String MOBILE_BANKING = "Mobile Banking";
    private static final String ONLINE_BANKING = "Online Banking";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SEGMENT = "segment";
    private static final String CUSTOMER_NAME = "customerName";
    private static final String CURRENCY = "currency";
    private static final String TRANSFER_TYPE = "transferType";
    private static final String SOURCE_OF_FUND = "sourceOfFund";
    private static final String MASKED_ACCOUNT = "maskedAccount";
    private static final String TO_ACCOUNT_NO = "toAccountNumber";
    private static final String BENEFICIARY_NICK_NAME = "beneficiaryNickname";
    private static final String AMOUNT = "amount";
    private static final String CONTACT_HTML_BODY_KEY = "contactHtmlBody";
    private static final String STATUS = "status";
    private static final String BANK_NAME = "bankName";
    private static final String CHANNEL_TYPE = "channelType";
    private static final String FACEBOOK_LINK = "facebookLink";
    private static final String YOUTUBE_LINK = "youtubeLink";
    private static final String INSTAGRAM_LINK = "instagramLink";
    private static final String TWITTER_LINK = "twitterLink";
    private static final String LINKED_IN_KEY = "linkedinLink";
    public static final String FACEBOOK = "facebook";
    public static final String INSTAGRAM = "instagram";
    public static final String TWITTER = "twitter";
    public static final String LINKED_IN = "linkedIn";
    public static final String YOUTUBE = "youtube";
    public static final String CUSTOMER = "Customer";
    public static final String DEFAULT_STR = "";

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private PostTransactionActivityService postTransactionActivityService;

    @Autowired
    private SendEmailActivity sendEmailActivity;

    @Autowired
    private MobCommonService mobCommonService;

    @Autowired
    private AsyncUserEventPublisher userEventPublisher;

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
            final EmailTemplateParameters emailTemplateParameters = getEmailTemplateParameters(requestMetaData.getChannel(), requestMetaData.getSegment());
            String channelType = requestMetaData.getChannel().equalsIgnoreCase(MOBILE) ? MOBILE_BANKING : ONLINE_BANKING;
            Map<String, String> templateValues = new HashMap<>();
            /* final String subject = String.format(emailParameters.getEmailSubject(), "LOCAL FT", channelType);
            templateValues.put(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR));
            templateValues.put(CUSTOMER_NAME, StringUtils.defaultIfBlank(requestMetaData.getUsername(), CUSTOMER));
            templateValues.put(TRANSFER_TYPE, StringUtils.defaultIfBlank("Local FT", DEFAULT_STR));
            templateValues.put(SOURCE_OF_FUND, StringUtils.defaultIfBlank("Account", DEFAULT_STR));
            templateValues.put(MASKED_ACCOUNT, StringUtils.defaultIfBlank("AE700330000011099270455", DEFAULT_STR));
            templateValues.put(TO_ACCOUNT_NO, StringUtils.defaultIfBlank("AE700330000011099270455", DEFAULT_STR));
            templateValues.put(BENEFICIARY_NICK_NAME, StringUtils.defaultIfBlank("Asmita Local", DEFAULT_STR));
            templateValues.put(CURRENCY, StringUtils.defaultIfBlank("AED", DEFAULT_STR) );
            templateValues.put(AMOUNT, StringUtils.defaultIfBlank(String.valueOf("10.00"), DEFAULT_STR));
            templateValues.put(STATUS, StringUtils.defaultIfBlank("success", DEFAULT_STR));*/
            final String subject = String.format(emailParameters.getEmailSubject(), fundTransferRequest.getTransferType(), channelType);
            templateValues.put(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR));
            templateValues.put(CUSTOMER_NAME, StringUtils.defaultIfBlank(requestMetaData.getUsername(), CUSTOMER));
            templateValues.put(TRANSFER_TYPE, StringUtils.defaultIfBlank(fundTransferRequest.getTransferType(), DEFAULT_STR));
            templateValues.put(SOURCE_OF_FUND, StringUtils.defaultIfBlank(fundTransferRequest.getSourceOfFund(), DEFAULT_STR));
            templateValues.put(MASKED_ACCOUNT, StringUtils.defaultIfBlank(doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
            templateValues.put(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(fundTransferRequest.getToAccount(), DEFAULT_STR));
            templateValues.put(BENEFICIARY_NICK_NAME, StringUtils.defaultIfBlank(fundTransferRequest.getBeneficiaryFullName(), DEFAULT_STR));
            templateValues.put(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getSourceCurrency(), DEFAULT_STR) );
            templateValues.put(AMOUNT, StringUtils.defaultIfBlank(String.valueOf(fundTransferRequest.getAmount()), DEFAULT_STR));
            templateValues.put(STATUS, StringUtils.defaultIfBlank(fundTransferRequest.getStatus(), DEFAULT_STR));
            templateValues.put(BANK_NAME, StringUtils.defaultIfBlank(emailTemplateParameters.getChannelIdentifier().getChannelName(), DEFAULT_STR) );
            templateValues.put(CHANNEL_TYPE, StringUtils.defaultIfBlank(channelType, DEFAULT_STR));
            templateValues.put(CONTACT_HTML_BODY_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getHtmlContactContents().getHtmlContent(),DEFAULT_STR));
            templateValues.put(FACEBOOK_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(FACEBOOK), DEFAULT_STR));
            templateValues.put(INSTAGRAM_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(INSTAGRAM), DEFAULT_STR));
            templateValues.put(TWITTER_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(TWITTER), DEFAULT_STR));
            templateValues.put(LINKED_IN_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(LINKED_IN), DEFAULT_STR));
            templateValues.put(YOUTUBE_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(YOUTUBE), DEFAULT_STR));


            emailRequest = SendEmailRequest.builder()
                    .fromEmailAddress(emailParameters.getFromEmailAddress())
                    .toEmailAddress(requestMetaData.getEmail())
                    .subject(subject)
                    .fromEmailName(emailParameters.getFromEmailName())
                    .templateName(getTemplateName(fundTransferRequest.getTransferType(),emailParameters))
                    .templateKeyValues(templateValues)
                    .isEmailPresent(true)
                    .build();
        }
        return PostTransactionActivityContext.<SendEmailRequest>builder().payload(emailRequest).postTransactionActivity(sendEmailActivity).build();
    }

    private String getTemplateName(String type, EmailParameters emailParameters){
        String templateName;
        if(LOCAL_EMAIL_TEMPLATE.equalsIgnoreCase(type)){
            templateName =   emailParameters.getLocalFundTransfer();
        } else {
            templateName = emailParameters.getOtherFundTransfer();
        }
        return templateName;
    }

    private EmailTemplateParameters getEmailTemplateParameters (String channel, String segment) throws JsonProcessingException {
        final List<ApplicationSettingDto> applicationSettingDtos = mobCommonService.getApplicationSettings(APPLICATION_SETTINGS_GROUP);
        ChannelDetails channelDetails = getChannelDetails(APPLICATION_SETTINGS_CHANNEL_LOOKUP, channel, applicationSettingDtos);
        Map<String, String> socialMedia = getSocialMediaLinks(APPLICATION_SETTINGS_SOCIAL_MEDIA_LINKS, channelDetails, applicationSettingDtos);
        EmailTemplateContactWebsiteContent htmlContent = getEmailTemplateContactWebsiteContent(APPLICATION_SETTINGS_CONTACT_US_HTML,segment, applicationSettingDtos);

        return EmailTemplateParameters.builder().channelIdentifier(channelDetails).htmlContactContents(htmlContent).socialMediaLinks(socialMedia).build();
    }

    private EmailTemplateContactWebsiteContent getEmailTemplateContactWebsiteContent(String contactUsHtmlKey, String segment, List<ApplicationSettingDto> applicationSettingDtos) throws JsonProcessingException {
        Optional<String> contactUsHtmlContentString = getValueFromApplicationSettingDto(applicationSettingDtos, contactUsHtmlKey);
        assertApplicationSettingPresent(contactUsHtmlContentString);
        TypeReference<List<EmailTemplateContactWebsiteContent>> typeReferenceContactUs = new TypeReference<List<EmailTemplateContactWebsiteContent>>() {
        };
        List<EmailTemplateContactWebsiteContent> emailTemplateContactWebsiteContents = getObjectFromClass(contactUsHtmlContentString.get(), typeReferenceContactUs);
        EmailTemplateContactWebsiteContent htmlContent = emailTemplateContactWebsiteContents
                .stream()
                .filter(details ->  details.getSegment().equals(segment))
                .findFirst().orElse(null);
        log.info("Contact us details from application settings. {} ", htmlContent);
        return htmlContent;
    }

    private Map<String, String> getSocialMediaLinks(String socialMediaLinkKey, ChannelDetails channelDetails, List<ApplicationSettingDto> applicationSettingDtos) throws JsonProcessingException {
        Optional<String> socialMediaString = getValueFromApplicationSettingDto(applicationSettingDtos, socialMediaLinkKey);
        assertApplicationSettingPresent(socialMediaString);
        TypeReference<List<SocialMediaLinks>> typeReferenceSocialMediaLinks = new TypeReference<List<SocialMediaLinks>>() {};
        List<SocialMediaLinks> socialMediaLinks = getObjectFromClass(socialMediaString.get(), typeReferenceSocialMediaLinks);
        Map<String, String> socialMedia = socialMediaLinks
                .stream()
                .filter(details ->  details.getChannelIdentifier().equals(channelDetails.getChannelName()))
                .collect(Collectors.toMap(k -> k.getSocialMediaName(), v -> v.getSocialMediaLink()));
        log.info("Social media details from application settings. {} ", socialMedia);
        return socialMedia;
    }

    private ChannelDetails getChannelDetails(String channelDetailsDbKey, String channel, List<ApplicationSettingDto> applicationSettingDtos) throws JsonProcessingException {
        Optional<String> channelIdentifierString = getValueFromApplicationSettingDto(applicationSettingDtos, channelDetailsDbKey);
        assertApplicationSettingPresent(channelIdentifierString);
        log.info("Parsing channel config string from application settings. {} ", channelIdentifierString.get());
        TypeReference<List<ChannelDetails>> typeReferenceChannelDetails = new TypeReference<List<ChannelDetails>>() {};
        List<ChannelDetails> channelConfig = getObjectFromClass(channelIdentifierString.get(), typeReferenceChannelDetails);
        ChannelDetails channelDetails = channelConfig
                .stream()
                .filter(details ->  details.getChannelKey().equalsIgnoreCase(channel))
                .findFirst().orElse(null);
        log.info("Channel details from application settings. {} ", channelDetails);
        return channelDetails;
    }


    private <T> T getObjectFromClass(String value, TypeReference<T> tClass) throws JsonProcessingException {
            return OBJECT_MAPPER.readValue(value, tClass);
    }

    private Optional<String> getValueFromApplicationSettingDto(List<ApplicationSettingDto> applicationSettingDtos, String key) {
        return applicationSettingDtos
                .stream()
                .filter(x -> key.equalsIgnoreCase(x.getSettingKey()))
                .map(ApplicationSettingDto::getSettingValue)
                .findFirst();
    }

    public void assertApplicationSettingPresent(Optional<String> jsonConfigOpt) {
        if (!jsonConfigOpt.isPresent()) {
            log.error("Exception while parsing the settings value json string from application settings. ", FundTransferEventType.APPLICATION_SETTING_KEY_NOT_FOUND);
        }
    }

    public String doMask(String strText) throws Exception {
        int total = strText.length();
        int endLength = 4;
        int maskLength = total - endLength;
        if (maskLength <= 0) {
            GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_NO_NOT_MASKED,
                    TransferErrorCode.ACCOUNT_NO_NOT_MASKED.getErrorMessage());
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            builder.append('X');
        }
        String masked = builder.append(strText, maskLength, total).toString();
        return masked;
    }

}
