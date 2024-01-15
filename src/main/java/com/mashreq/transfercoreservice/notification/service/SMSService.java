package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.notification.client.freemarker.TemplateRequest;
import com.mashreq.notification.client.freemarker.TemplateType;
import com.mashreq.notification.client.notification.service.NotificationService;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.client.RequestMetadataMapper;
import com.mashreq.transfercoreservice.config.notification.SMSConfig;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.SMSObject;
import com.mashreq.transfercoreservice.notification.model.SMSResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


/**
 * This class sends sms notifications for payment successfully
 * placed to middleware as well as rejected cases.
 *
 * @author PallaviG
 */
@Slf4j
@Service
public class SMSService  {

    @Autowired
    NotificationClient notificationClient;

    @Autowired
    SMSConfig smsConfig;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private NotificationService notificationService;
    private static final String MOBILE = "MOBILE";
    private static final String WEB = "WEB";
    private static final String NEOMOBILE = "NEOMOBILE";
    private static final String NEOWEB = "NEOWEB";
    private static final Collection<String> MASHREQ_RETAIL_CHANNELS = CollectionUtils.unmodifiableCollection(Arrays.asList(MOBILE, WEB));
    private static final Collection<String> MASHREQ_NEO_CHANNELS = CollectionUtils.unmodifiableCollection(Arrays.asList(NEOMOBILE, NEOWEB));
    private static final String PL_SI_CREATION = "pl_si_creation";
    private static final String BENEFICIARY_NAME = "beneficiaryName";
    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static final String BUSINESS_TYPE = "RETAIL";
    public static final String FUND_TRANSFER = "fund_transfer";
    public static final String CURRENCY = "currency";
    public static final String AMOUNT = "amount";
    public static final String TRANSACTION_REFERENCE = "txnRef";

    @Value("${default.notification.language}")
    private String defaultLanguage;

    private static final String leftToRight = "\u200E";

    private static final String ARABIC = "AR";
    /**
     * service method to prepare sms template based on type of sms and send sms
     */

    @TrackExecTimeAndResult
    public boolean sendSMS(CustomerNotification customerNotification, String type, RequestMetaData metaData, int retryCount, String phoneNo) {
        String logPrefix = metaData.getPrimaryCif() + ", retryCount: " + retryCount;
        // Implementing temporary solution with new notification-client jar for type - plSiCreation and ownAccountTransactionInitiated
        if(type.contains("PL") && type.contains("CREATION")) {
            TemplateRequest templateRequest = buildSmsTemplate(PL_SI_CREATION,metaData,customerNotification,phoneNo)
                    .params(BENEFICIARY_NAME,customerNotification.getBeneficiaryName())
                    // Added 'leftToRight' to correct word alignment for arabic SMS
                    .params(ACCOUNT_NUMBER,isArabic() ? leftToRight + emailUtil.doMask(customerNotification.getCreditAccount()) + leftToRight : emailUtil.doMask(customerNotification.getCreditAccount()))
                    .configure();
            try {
                notificationService.sendNotification(templateRequest);
            }catch (Exception e){
                log.error("sendNotification - SMS cannot be sent for username - {} exception - {} ",
                        htmlEscape(metaData.getUsername()),e.getMessage());
            }
            return true;
        } else if (type.contains("FUNDS TRANSFER")){
            TemplateRequest templateRequest = buildSmsTemplate(FUND_TRANSFER,metaData,customerNotification,phoneNo)
                    .params(CURRENCY,customerNotification.getCurrency())
                    .params(AMOUNT,customerNotification.getAmount())
                    .params(TRANSACTION_REFERENCE,customerNotification.getTxnRef())
                    .configure();
            try {
                notificationService.sendNotification(templateRequest);
            }catch (Exception e){
                log.error("sendNotification - SMS cannot be sent for username - {} exception - {} ",
                        htmlEscape(metaData.getUsername()),e.getMessage());
            }
            return true;
        }else {
            String message = smsConfig.getSMSTemplate(type, customerNotification);
            return sendSMS(message, phoneNo, metaData, logPrefix);
        }
    }

    private TemplateRequest.SMSBuilder buildSmsTemplate(String templateName,RequestMetaData metaData,CustomerNotification customerNotification,String phoneNo) {
        return TemplateRequest.smsBuilder()
                .templateType(TemplateType.SMS)
                .templateName(templateName)
                .businessType(BUSINESS_TYPE)
                .channel(customerNotification.getChannel())
                .country(metaData.getCountry())
                .language(defaultLanguage)
                .segment(customerNotification.getSegment().getName())
                .mobileNumber(phoneNo);
    }

    /**
     * prepares sms request and calls sms notification endpoint
     *
     * @param message
     * @param phoneNumber
     * @return
     */
    private boolean sendSMS(String message, String phoneNumber, RequestMetaData metaData, String logPrefix) {
        Map<String,String> headerMap = RequestMetadataMapper.collectRequestMetadataAsMap(metaData);
        log.info("{}, smsMessage: {}, phoneNumber: {}. SMS being sent.", htmlEscape(logPrefix), htmlEscape(message), htmlEscape(phoneNumber));
        SMSResponse smsResponse = notificationClient.sendSMS(headerMap, createSmsObject(message, phoneNumber, metaData));
        log.info("{}, smsResponse: {}, SMS response received.", htmlEscape(logPrefix), htmlEscape(smsResponse));
        return smsResponse != null && smsResponse.getStatusCode() != null && "SUCCESS".equalsIgnoreCase(smsResponse.getStatusCode());
    }

    /**
     * prepares and returns sms request
     *
     * @param message
     * @param phoneNumber
     * @return
     */
    private SMSObject createSmsObject(String message, String phoneNumber, RequestMetaData metaData) {
		SMSObject smsObject = new SMSObject();
		smsObject.setMessage(message);
		smsObject.setMobileNumber(phoneNumber);
		if (MASHREQ_RETAIL_CHANNELS.contains(metaData.getChannel())) {
			smsObject.setServiceId(smsConfig.getMashreqServiceId());
		} else if (MASHREQ_NEO_CHANNELS.contains(metaData.getChannel())) {
			smsObject.setServiceId(smsConfig.getNeoServiceId());
		} else {
			smsObject.setServiceId(smsConfig.getServiceId());
		}
		smsObject.setPriority(smsConfig.getPriority());
		return smsObject;
    }

    private boolean isArabic() {
        return ARABIC.equalsIgnoreCase(defaultLanguage);
    }
}