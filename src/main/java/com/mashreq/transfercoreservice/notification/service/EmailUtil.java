package com.mashreq.transfercoreservice.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

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
    public static final String PL_TYPE = "plType";
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
    public static final String CUSTOMER_DEFAULT = "Customer";
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

    public static final String RECIPIENTS = "recipients";
    public static final String REQUESTED_AMOUNT = "Requested amount ";
    public static final String MESSAGE = "message";
    public static final String VALUE = "Value";
    public static final String PROXY = "proxy";
    public static final String FROM_ACCOUNT = "fromAccount";
    public static final String ALTERNATE_STEPS = "alternateSteps";
    public static final String ALTERNATE_STEPS_IF_ANY = "alternateStepsIfAny";
    public static final String LOCAL_FUND_TRANSFER = "mt_within_own_accounts";
    public static final String GOLD_SILVER_BUY_SUCCESS = "mt_buy_gold_silver_success";
    public static final String GOLD_SILVER_SELL_SUCCESS = "mt_sell_gold_silver_success";
    public static final String PL_SI_FUND_TRANSFER = "mt_pl_creation";
    public static final String OTHER_FUND_TRANSFER = "mt_other_accounts";
    public static final String BUSINESS_TYPE = "RETAIL";

    public static final String MT_NPSS_EMAIL_PROXY_UPDATE = "mt_npss_email_proxy_update";
    public static final String EMAIL_PROXY = "emailId";
    public static final String MT_NPSS_PAYMENT_SUCCESSFUL = "mt_npss_payment_successful";
    public static final String MT_NPSS_PAYMENT_FAILURE = "mt_npss_payment_failure";
    public static final String MT_NPSS_REQUEST_SENT = "mt_npss_request_sent";
    public static final String MT_NPSS_REQUEST_SENT_MULTIPLE = "mt_npss_request_sent_multiple";
    public static final String MT_NPSS_REQUEST_SENT_MULTIPLE_FAIL = "mt_npss_request_sent_multiple_failure";
    public static final String MT_NPSS_ENROLLMENT = "mt_npss_enrollment";
    public static final String NOT_APPLICABLE = "NA";
    public static final String MT_NPSS_REQUEST_RECEIVED = "mt_npss_request_received";
    public static final String MT_NPSS_REQUEST_SENT_DECLINED = "mt_npss_request_sent_declined";
    public static final String MT_NPSS_REQUEST_SENT_EXPITED = "mt_npss_request_sent_expired";
    public static final String MT_NPSS_MOBILE_PHONE_NUMBER_CHANGED = "mt_npss_mobile_number_changed";
    public static final String MT_NPSS_REQUEST_SENT_FAILURE = "mt_npss_request_sent_failure";
    public static final String PAYMENT_NOTE = "paymentNote";
    public static final String RECEIVER_NAME = "receiverName";
    public static final String SENDER_NAME = "senderName";
    public static final String ZERO = "0";
    public static final String defaultLanguage = "EN";
    public static final String BENEFICIARY_BANK_BRANCH_NAME = "branchName";

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

    public static String formattedAmount(BigDecimal amount){
        if (amount != null) {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            decimalFormat.setGroupingUsed(true);
            decimalFormat.setGroupingSize(3);
            decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
            return decimalFormat.format(amount);
        }
        return DEFAULT_STR;
    }
}
