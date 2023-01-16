package com.mashreq.transfercoreservice.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.model.ApplicationSettingDto;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.ChannelDetails;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateContactWebsiteContent;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateParameters;
import com.mashreq.transfercoreservice.notification.model.SocialMediaLinks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EmailUtil {

    public static final String APPLICATION_SETTINGS_CHANNEL_LOOKUP = "EMAIL_TEMPLATE_CHANNEL_LOOKUP";
    public static final String APPLICATION_SETTINGS_SOCIAL_MEDIA_LINKS = "EMAIL_TEMPLATE_SOCIAL_MEDIA_LINKS";
    public static final String APPLICATION_SETTINGS_CONTACT_US_HTML = "EMAIL_TEMPLATE_CONTACT_US_HTML_CONTENT";
    public static final String APPLICATION_SETTINGS_GROUP = "EMAIL_SETTINGS";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String SOURCE_OF_FUND_ACCOUNT = "Account";

    public static final String MOBILE = "MOBILE";
    public static final String MOBILE_BANKING = "Mobile Banking";
    public static final String ONLINE_BANKING = "Online Banking";

    public static final String SEGMENT = "segment";
    public static final String CUSTOMER_NAME = "customerName";
    public static final String CURRENCY = "currency";
    public static final String LOCAL_CURRENCY = "localCurrency";
    public static final String EXCHANGE_RATE = "exchangeRate";
    public static final String ACCOUNT_CURRENCY = "accountCurrency";
    public static final String DESTINATION_ACCOUNT_CURRENCY = "destinationAccCurrency";
    public static final String TRANSFER_TYPE = "transferType";
    public static final String SOURCE_OF_FUND = "sourceOfFund";
    public static final String MASKED_ACCOUNT = "maskedAccount";
    public static final String TO_ACCOUNT_NO = "toAccountNumber";
    public static final String BENEFICIARY_NICK_NAME = "beneficiaryNickname";
    public static final String BENEFICIARY_BANK_NAME = "beneBankName";
    public static final String BENEFICIARY_BANK_COUNTRY = "beneBankCountry";
    public static final String CUSTOMER_CARE_NO = "customerCareNo";
    public static final String TRANSACTION_DATE = "transactionDate";
    public static final String EXECUTION_DATE = "executionDate";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FREQUENCY = "frequency";
    public static final String TRANSACTION_TYPE = "transactionType";
    public static final String AMOUNT = "amount";
    public static final String SOURCE_AMOUNT = "sourceAmount";
    public static final String BANK_FEES = "bankFees";
    public static final String CONTACT_HTML_BODY_KEY = "contactHtmlBody";
    public static final String STATUS = "status";
    public static final String BANK_NAME = "bankName";
    public static final String CHANNEL_TYPE = "channelType";
    public static final String FACEBOOK_LINK = "facebookLink";
    public static final String YOUTUBE_LINK = "youtubeLink";
    public static final String INSTAGRAM_LINK = "instagramLink";
    public static final String TWITTER_LINK = "twitterLink";
    public static final String LINKED_IN_KEY = "linkedinLink";
    public static final String FACEBOOK = "facebook";
    public static final String INSTAGRAM = "instagram";
    public static final String TWITTER = "twitter";
    public static final String LINKED_IN = "linkedIn";
    public static final String YOUTUBE = "youtube";
    public static final String CUSTOMER = "Customer";
    public static final String DEFAULT_STR = "";
    public static final String FX_DEAL_CODE = "fxDealCode";
    public static final String ORDER_TYPE = "orderType";
    public static final String TXN_AMOUNT="txn_amount";
    public static final String STATUS_SUCCESS="Success";
    public static final String COMMA_SEPARATOR = ",";
    public static final String DECIMAL_POS = "%.2f";
    public static final String SEGMENT_SIGN_OFF_COMPANY_NAME="segmentSignOffCompanyName";
    public static final String EMAIL_TEMPLATE_COPYRIGHT_YEAR_KEY = "copyrightYear";
    public static final String BANK_NAME_FOOTER = "bankNameInFooter";
    public static final String BANK_NAME_FOOTER_DESC = "bankNameInFooterDesc";
    public static final String AED = "AED";

    public static final String CONTACT_NAME = "contactName";
    public static final String REFERENCE_NUMBER = "referenceNumber";
    public static final String SENT_TO = "sentTo";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String REASON_FOR_FAILURE = "reasonForFailure";

    @Autowired
    private MobCommonService mobCommonService;

    @Autowired
    private DigitalUserSegment digitalUserSegment;

    public EmailTemplateParameters getEmailTemplateParameters (String channel, String segment) throws JsonProcessingException {
        final List<ApplicationSettingDto> applicationSettingDtos = mobCommonService.getApplicationSettings(APPLICATION_SETTINGS_GROUP);
        ChannelDetails channelDetails = getChannelDetails(APPLICATION_SETTINGS_CHANNEL_LOOKUP, channel, applicationSettingDtos);
        Map<String, String> socialMedia = getSocialMediaLinks(APPLICATION_SETTINGS_SOCIAL_MEDIA_LINKS, channelDetails, applicationSettingDtos);
        EmailTemplateContactWebsiteContent htmlContent = getEmailTemplateContactWebsiteContent(APPLICATION_SETTINGS_CONTACT_US_HTML,segment, applicationSettingDtos);
        Segment segmentObj = digitalUserSegment.getCustomerCareInfo(segment);
        return EmailTemplateParameters.builder().channelIdentifier(channelDetails).htmlContactContents(htmlContent)
                .socialMediaLinks(socialMedia).segment(segmentObj).build();
    }

    private   EmailTemplateContactWebsiteContent getEmailTemplateContactWebsiteContent(String contactUsHtmlKey, String segment, List<ApplicationSettingDto> applicationSettingDtos) throws JsonProcessingException {
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

    public String doMask(String strText)  {
        int total = strText.length();
        int endLength = 4;
        int maskLength = total - endLength;
        if (maskLength <= 0) {
            GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_NO_NOT_MASKED,
                    TransferErrorCode.ACCOUNT_NO_NOT_MASKED.getErrorMessage());
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            builder.append('*');
        }
        String masked = builder.append(strText, maskLength, total).toString();
        return masked;
    }

    public String capitalizeFully(String str) {
        StringBuffer s = new StringBuffer();

        char ch = ' ';
        for (int i = 0; i < str.length(); i++) {

            if (ch == ' ' && str.charAt(i) != ' ')
                s.append(Character.toUpperCase(str.charAt(i)));
            else
                s.append(str.charAt(i));
            ch = str.charAt(i);
        }
        return s.toString().trim();
    }

    public static String formattedAmount(BigDecimal bigDecimal){
        int firstCount = 0;
        int nextCount = 0;
        boolean isFirst = false;
        String decimalValue = String.format(DECIMAL_POS, bigDecimal);
        String[] values = decimalValue.split("\\.");
        StringBuilder builder = new StringBuilder();
        char[] charArray = values[0].toCharArray();
        int count = charArray.length-1;
        while (count >= 0){
            if(isFirst){
                if(nextCount == 2){
                    builder.append(COMMA_SEPARATOR);
                    nextCount = 0;
                } else {
                    nextCount++;
                }
            } else {
                if(firstCount == 3){
                    builder.append(COMMA_SEPARATOR);
                    isFirst = true;
                } else {
                    firstCount++;
                }
            }
            builder.append(charArray[count]);
            count--;
        }
        builder.reverse().append(".").append(values[1]);
        return builder.toString();
    }
}
