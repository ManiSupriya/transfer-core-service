//package com.mashreq.transfercoreservice.notification.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
//import com.mashreq.mobcommons.services.http.RequestMetaData;
//import com.mashreq.ms.exceptions.GenericExceptionHandler;
//import com.mashreq.transfercoreservice.client.NotificationClient;
//import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
//import com.mashreq.transfercoreservice.config.notification.EmailTemplate;
//import com.mashreq.transfercoreservice.config.notification.EmailTemplateHelper;
//import com.mashreq.transfercoreservice.model.ApplicationSetting;
//import com.mashreq.transfercoreservice.notification.model.*;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.mapstruct.ap.shaded.freemarker.template.TemplateException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;
//import static com.mashreq.transfercoreservice.errors.TransferErrorCode.APPLICATION_KEY_NOT_FOUND;
//
//
///**
// * This class sends email notifications for payment in_process, successfully
// * placed to middleware as well as rejected cases.
// *
// * @author PallaviG
// */
//@Slf4j
//@Service
//public class EmailService {
//
//    private static final String MOBILE = "MOBILE";
//    private static final String MOBILE_BANKING = "Mobile Banking";
//    private static final String ONLINE_BANKING = "Online Banking";
//    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//    private static final String EMAIL_TEMPLATE_CURRENCY_KEY = "currency";
//    private static final String EMAIL_TEMPLATE_CONTACT_HTML_BODY_KEY = "contactHtmlBody";
//    private static final String EMAIL_TEMPLATE_FACEBOOK_KEY = "facebookLink";
//    private static final String EMAIL_TEMPLATE_YOUTUBE_KEY = "youtubeLink";
//    private static final String EMAIL_TEMPLATE_INSTAGRAM_KEY = "instagramLink";
//    private static final String EMAIL_TEMPLATE_TWITTER_KEY = "twitterLink";
//    private static final String EMAIL_TEMPLATE_LINKEDIN_KEY = "linkedinLink";
//    private static final String EMAIL_TEMPLATE_PAID_AMOUNT_KEY = "Amount";
//    private static final String EMAIL_TEMPLATE_CHANNEL_TYPE_KEY = "channelType";
//    private static final String EMAIL_TEMPLATE_CUSTOMER_NAME_KEY = "customerName";
//    private static final String EMAIL_TEMPLATE_SEGMENT_KEY = "segment";
//    private static final String EMAIL_TEMPLATE_CHANNEL_IDENTIFIER_KEY = "bankName";
//    private static final String APPLICATION_SETTINGS_CHANNEL_LOOKUP = "EMAIL_TEMPLATE_CHANNEL_LOOKUP";
//    private static final String APPLICATION_SETTINGS_SOCIAL_MEDIA_LINKS = "EMAIL_TEMPLATE_SOCIAL_MEDIA_LINKS";
//    private static final String APPLICATION_SETTINGS_CONTACT_US_HTML = "EMAIL_TEMPLATE_CONTACT_US_HTML_CONTENT";
//    private static final String APPLICATION_SETTINGS_GROUP = "EMAIL_SETTINGS";
//    private static final String EMAIL_TEMPLATE_STATUS_KEY = "Status";
//    private static final String EMAIL_TEMPLATE_SOURCE_FUND_KEY = "sourceOfFund";
//    private static final String EMAIL_TEMPLATE_SOURCE_FUND_VALUE = "ACCOUNT";
//    private static final String EMAIL_TEMPLATE_FROM_ACCOUNT_KEY = "maskedAccount";
//    private static final String EMAIL_TEMPLATE_TO_ACCOUNT_KEY = "toAccountNumber";
//    private static final String EMAIL_TEMPLATE_STATUS_VALUE = "Success";
//    private static final String EMAIL_TEMPLATE_TRANSFER_TYPE_KEY = "transferType";
//    @Autowired
//    NotificationClient notificationClient;
//    @Autowired
//    MobCommonService mobCommonService;
//    @Autowired
//    private EmailTemplate emailTemplate;
//    @Autowired
//    private EmailTemplateHelper emailTemplateHelper;
//
//    private static String maskString(String strText, int start, int end, char maskChar)
//            throws Exception {
//
//        if (strText == null || strText.equals(""))
//            return "";
//
//        if (start < 0)
//            start = 0;
//
//        if (end > strText.length())
//            end = strText.length();
//
//        if (start > end)
//            throw new Exception("End index cannot be greater than start index");
//
//        int maskLength = end - start;
//
//        if (maskLength == 0)
//            return strText;
//
//        StringBuilder sbMaskString = new StringBuilder(maskLength);
//
//        for (int i = 0; i < maskLength; i++) {
//            sbMaskString.append(maskChar);
//        }
//
//        return strText.substring(0, start)
//                + sbMaskString.toString()
//                + strText.substring(start + maskLength);
//    }
//
//    static String capitalizeFully(String str) {
//        StringBuffer s = new StringBuffer();
//
//        char ch = ' ';
//        for (int i = 0; i < str.length(); i++) {
//
//            if (ch == ' ' && str.charAt(i) != ' ')
//                s.append(Character.toUpperCase(str.charAt(i)));
//            else
//                s.append(str.charAt(i));
//            ch = str.charAt(i);
//        }
//        return s.toString().trim();
//    }
//
//    /**
//     * service method to prepare email template based on type of email and send email
//     */
//    @TrackExecTimeAndResult
//    public boolean sendEmail(CustomerNotification customerNotification, String type, RequestMetaData metaData, int retryCount) throws IOException, TemplateException {
//        String logPrefix = metaData.getPrimaryCif() + ", retryCount: " + retryCount;
//        String template = emailTemplate.getEmailTemplate(type);
//        Map<String, String> templateValues = getEmailTemplateValues(customerNotification, metaData);
//        String emailBody = emailTemplateHelper.getEmailTemplate(template, templateValues);
//        String subject = emailTemplate.getEmailSubjectTmpl(metaData.getChannel().contains(MOBILE) ? MOBILE_BANKING : ONLINE_BANKING, type);
//        return send(emailBody, metaData.getEmail(), logPrefix, emailTemplate, subject);
//    }
//
//    /**
//     * prepares email request and calls email notification endpoint
//     */
//    private boolean send(String emailBody, String emailAddress, String logPrefix, EmailTemplate emailTemplate, String subject) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        EmailRequest emailRequest = new EmailRequest();
//        emailRequest.setFromEmailAddress(emailTemplate.getFromEmailAddress());
//        emailRequest.setFromEmailName(emailTemplate.getFromEmailName());
//        emailRequest.setToEmailAddress(emailAddress);
//        emailRequest.setSubject(subject);
//        emailRequest.setText(emailBody);
//        log.info("{}, emailId = {}, templateValueMap = {}, Email Request being sent.", htmlEscape(logPrefix), htmlEscape(emailAddress), htmlEscape(emailBody));
//        EmailResponse emailResponse = notificationClient.sendEmail(emailRequest);
//        log.info("{}, emailResponse = {}", htmlEscape(logPrefix), htmlEscape(emailResponse.toString()));
//        return emailResponse != null && emailResponse.isSuccess();
//    }
//
//    /**
//     * prepares and returns email template values
//     */
//
//    private Map<String, String> getEmailTemplateValues(CustomerNotification customerNotification, RequestMetaData metaData) {
//
//        final EmailTemplateParameters emailTemplateParameters = getEmailTemplateParameters(metaData.getChannel(), metaData.getSegment());
//        String channelString = metaData.getChannel().contains(MOBILE) ? MOBILE_BANKING : ONLINE_BANKING;
//        Map<String, String> templateValues = new HashMap<>();
//
//        //templateValues.put(EMAIL_TEMPLATE_CURRENCY_KEY, StringUtils.defaultIfBlank(customerNotification.getSendCurrency(), ""));
//        templateValues.put(EMAIL_TEMPLATE_PAID_AMOUNT_KEY, StringUtils.defaultIfBlank(customerNotification.getAmount(), ""));
//        templateValues.put(EMAIL_TEMPLATE_CUSTOMER_NAME_KEY, StringUtils.defaultIfBlank(capitalizeFully(metaData.getUsername().toLowerCase()), "Customer"));
//        templateValues.put(EMAIL_TEMPLATE_SEGMENT_KEY, StringUtils.defaultIfBlank(metaData.getSegment(), ""));
//        templateValues.put(EMAIL_TEMPLATE_CHANNEL_IDENTIFIER_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getChannelIdentifier().getChannelName(), ""));
//        templateValues.put(EMAIL_TEMPLATE_CHANNEL_TYPE_KEY, StringUtils.defaultIfBlank(channelString, ""));
//
//        templateValues.put(EMAIL_TEMPLATE_CONTACT_HTML_BODY_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getHtmlContactContents().getHtmlContent(), ""));
//        templateValues.put(EMAIL_TEMPLATE_FACEBOOK_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get("facebook"), ""));
//        templateValues.put(EMAIL_TEMPLATE_INSTAGRAM_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get("instagram"), ""));
//        templateValues.put(EMAIL_TEMPLATE_TWITTER_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get("twitter"), ""));
//        templateValues.put(EMAIL_TEMPLATE_LINKEDIN_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get("linkedIn"), ""));
//        templateValues.put(EMAIL_TEMPLATE_YOUTUBE_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get("youtube"), ""));
//        templateValues.put(EMAIL_TEMPLATE_STATUS_KEY, EMAIL_TEMPLATE_STATUS_VALUE);
//        templateValues.put(EMAIL_TEMPLATE_SOURCE_FUND_KEY, EMAIL_TEMPLATE_SOURCE_FUND_VALUE);
////        templateValues.put(EMAIL_TEMPLATE_ACCOUNT_KEY, StringUtils.defaultIfBlank(getMaskedAccount(customerNotification.getSenAccountNo()), ""));
////        templateValues.put(EMAIL_TEMPLATE_CONTACT_NUMBER_KEY, StringUtils.defaultIfBlank(getMaskedNumber(customerNotification.getBenMobile()), ""));
//        return templateValues;
//    }
//
//    private EmailTemplateParameters getEmailTemplateParameters(String channel, String segment) {
//        final List<ApplicationSetting> applicationSettingDtos = mobCommonService.getApplicationSettings(APPLICATION_SETTINGS_GROUP);
//        ChannelDetails channelDetails = getChannelDetails(APPLICATION_SETTINGS_CHANNEL_LOOKUP, channel, applicationSettingDtos);
//        Map<String, String> socialMedia = getSocialMediaLinks(APPLICATION_SETTINGS_SOCIAL_MEDIA_LINKS, channelDetails, applicationSettingDtos);
//        EmailTemplateContactWebsiteContent htmlContent = getEmailTemplateContactWebsiteContent(APPLICATION_SETTINGS_CONTACT_US_HTML, segment, applicationSettingDtos);
//
//        return EmailTemplateParameters.builder().channelIdentifier(channelDetails).htmlContactContents(htmlContent).socialMediaLinks(socialMedia).build();
//    }
//
//    private ChannelDetails getChannelDetails(String channelDetailsDbKey, String channel, List<ApplicationSetting> applicationSetting) {
//        Optional<String> channelIdentifierString = getValueFromApplicationSettingDto(applicationSetting, channelDetailsDbKey);
//        if (!channelIdentifierString.isPresent()) {
//            log.info("failed to get application key for {} ", channelDetailsDbKey);
//            GenericExceptionHandler.handleError(APPLICATION_KEY_NOT_FOUND, APPLICATION_KEY_NOT_FOUND.getErrorMessage());
//        }
//        log.info("Parsing channel config string from application settings. {} ", channelIdentifierString.get());
//        TypeReference<List<ChannelDetails>> typeReferenceChannelDetails = new TypeReference<List<ChannelDetails>>() {
//        };
//        List<ChannelDetails> channelConfig = getObjectFromClass(channelIdentifierString.get(), typeReferenceChannelDetails);
//        ChannelDetails channelDetails = channelConfig
//                .stream()
//                .filter(details -> details.getChannelKey().equals(channel))
//                .findFirst().orElse(null);
//        log.info("Channel details from application settings. {} ", channelDetails);
//        return channelDetails;
//    }
//
//    private Map<String, String> getSocialMediaLinks(String socialMediaLinkKey, ChannelDetails channelDetails, List<ApplicationSetting> applicationSetting) {
//        Optional<String> socialMediaString = getValueFromApplicationSettingDto(applicationSetting, socialMediaLinkKey);
//        if (!socialMediaString.isPresent()) {
//            log.info("failed to get application key for {} ", socialMediaLinkKey);
//            GenericExceptionHandler.handleError(APPLICATION_KEY_NOT_FOUND, APPLICATION_KEY_NOT_FOUND.getErrorMessage());
//        }
//        TypeReference<List<SocialMediaLinks>> typeReferenceSocialMediaLinks = new TypeReference<List<SocialMediaLinks>>() {
//        };
//        List<SocialMediaLinks> socialMediaLinks = getObjectFromClass(socialMediaString.get(), typeReferenceSocialMediaLinks);
//        Map<String, String> socialMedia = socialMediaLinks
//                .stream()
//                .filter(details -> details.getChannelIdentifier().equals(channelDetails.getChannelName()))
//                .collect(Collectors.toMap(k -> k.getSocialMediaName(), v -> v.getSocialMediaLink()));
//        log.info("Social media details from application settings. {} ", socialMedia);
//        return socialMedia;
//    }
//
//    private EmailTemplateContactWebsiteContent getEmailTemplateContactWebsiteContent(String contactUsHtmlKey, String segment, List<ApplicationSetting> applicationSetting) {
//        Optional<String> contactUsHtmlContentString = getValueFromApplicationSettingDto(applicationSetting, contactUsHtmlKey);
//        if (!contactUsHtmlContentString.isPresent()) {
//            log.info("failed to get application key for {} ", contactUsHtmlKey);
//            GenericExceptionHandler.handleError(APPLICATION_KEY_NOT_FOUND, APPLICATION_KEY_NOT_FOUND.getErrorMessage());
//        }
//        TypeReference<List<EmailTemplateContactWebsiteContent>> typeReferenceContactUs = new TypeReference<List<EmailTemplateContactWebsiteContent>>() {
//        };
//        List<EmailTemplateContactWebsiteContent> emailTemplateContactWebsiteContents = getObjectFromClass(contactUsHtmlContentString.get(), typeReferenceContactUs);
//        EmailTemplateContactWebsiteContent htmlContent = emailTemplateContactWebsiteContents
//                .stream()
//                .filter(details -> details.getSegment().equals(segment))
//                .findFirst().orElse(null);
//        log.info("Contact us details from application settings. {} ", htmlContent);
//        return htmlContent;
//    }
//
//    private Optional<String> getValueFromApplicationSettingDto(List<ApplicationSetting> applicationSettingDtos, String key) {
//        return applicationSettingDtos
//                .stream()
//                .filter(x -> key.equals(x.getSettingKey()))
//                .map(ApplicationSetting::getSettingValue)
//                .findFirst();
//    }
//
//    private <T> T getObjectFromClass(String value, TypeReference<T> tClass) {
//        try {
//            return OBJECT_MAPPER.readValue(value, tClass);
//        } catch (JsonProcessingException e) {
//            GenericExceptionHandler.logOnly(e, "Exception while parsing the settings value json string from application settings.");
//        }
//        return null;
//    }
//
//    private String getMaskedNumber(String benMobile) {
//
//        try {
//            return maskString(benMobile, 4, benMobile.length() - 4, 'X');
//        } catch (Exception e) {
//            GenericExceptionHandler.logOnly(e, "Exception while masking mobile no");
//        }
//        return benMobile;
//    }
//
//    private String getMaskedAccount(String accountNumber) {
//        try {
//            return maskString(accountNumber, 0, accountNumber.length() - 4, 'X');
//        } catch (Exception e) {
//            GenericExceptionHandler.logOnly(e, "Exception while masking account no");
//        }
//        return accountNumber;
//    }
//}