package com.mashreq.transfercoreservice.notification.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Parameters to configure e-mail.
 */
@Getter @Setter
public class EmailParameters {

    private String emailSubject;
    private String fromEmailName;
    private String fromEmailAddress;
    private String localFundTransfer;
    private String otherFundTransfer;
    private String plSiFundTransfer;
    private String goldSilverBuySuccess;
    private String goldSilverSellSuccess;
    private String sellGoldSilverSubject;
    private String buyGoldSilverSubject;

    private String enrolmentConfirmSubject;

    private String enrolmentConfirm;
    private String paymentFail;
    private String paymentReceived;
    private String paymentRequestReceived;
    private String paymentRequestSent;
    private String paymentSuccess;

    public String getEmailTemplate(String type) {
        if (type.equalsIgnoreCase(NotificationType.LOCAL)) {
            return localFundTransfer;
        }
        else if(type.equalsIgnoreCase(NotificationType.GOLD_SILVER_BUY_SUCCESS)){
            return goldSilverBuySuccess;
        }
        else if(type.equalsIgnoreCase(NotificationType.GOLD_SILVER_SELL_SUCCESS)){
            return goldSilverSellSuccess;
        }
        else if(type.contains("PL") || type.contains("SI")){
            return plSiFundTransfer;
        }
        else return otherFundTransfer;
    }

    public String getEmailSubject(String type,String transferType,String channel) {
        if(type.equalsIgnoreCase(NotificationType.GOLD_SILVER_BUY_SUCCESS)){
            return String.format(buyGoldSilverSubject, transferType,channel);
        }
        else if(type.equalsIgnoreCase(NotificationType.GOLD_SILVER_SELL_SUCCESS)){
            return String.format(sellGoldSilverSubject, transferType,channel);
        }

        else return String.format(emailSubject, transferType,channel);
    }

    public String getNpssEmailTemplate(String type){
        if (type.equalsIgnoreCase(NotificationType.CUSTOMER_ENROLMENT)) {
            return enrolmentConfirm;
        }
        else if(type.equalsIgnoreCase(NotificationType.PAYMENT_FAIL)){
            return paymentFail;
        }
        else if(type.equalsIgnoreCase(NotificationType.PAYMENT_RECEIVED)){
            return paymentReceived;
        }
        else if(type.equalsIgnoreCase(NotificationType.PAYMENT_REQUEST_RECEIVED)){
            return paymentRequestReceived;
        } else if(type.equalsIgnoreCase(NotificationType.PAYMENT_REQUEST_SENT)){
            return paymentRequestSent;
        }
        else return paymentSuccess;
    }

    public String getNpssEmailSubject(String type,String transferType,String channel) {
        if(type.equalsIgnoreCase(NotificationType.CUSTOMER_ENROLMENT)){
            return String.format(enrolmentConfirmSubject, transferType,channel);
        }

        else return String.format(emailSubject, transferType,channel);
    }
}
