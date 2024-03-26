package com.mashreq.transfercoreservice.cardlesscash.advice;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequestV2;
import com.mashreq.transfercoreservice.cardlesscash.service.DigitalUserSegmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mashreq.twofa.advice.TwoFaAdvice;
import org.mashreq.twofa.advice.response.TwoFaAdviceResponse;
import org.mashreq.twofa.advice.response.TwoFaSmsParams;
import org.mashreq.twofa.domain.TwoFaType;
import org.mashreq.twofa.utils.http.TwoFaRequestHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;

@Slf4j
@RequiredArgsConstructor
@Component
public class CardlessCashGenerationTwoFaAdvice implements TwoFaAdvice<CardLessCashGenerationRequestV2> {

    @Value("${twofa.otp.cardless-cash.msg}")
    private String cardlessCashTxnOtpMsg;

    private final TwoFaRequestHelper twoFaRequestHelper;
    private final DigitalUserSegmentService digitalUserSegmentService;


    @Override
    public boolean isTwoFaRequired(CardLessCashGenerationRequestV2 body) {
        return true;
    }

    @Override
    public TwoFaAdviceResponse overrideAdvice(CardLessCashGenerationRequestV2 request) {
        var customerCareContact = getCustomerCareNumber(constructRequestMetaData());
        var message = StringUtils.replaceEach(cardlessCashTxnOtpMsg, new String[]{"{currency}", "{amount}", "{masked_account_no}", "{paymnt_source}", "{txn_type}", "{callcenter_contact}"},
                new String[]{request.getCurrencyCode(), String.valueOf(request.getAmount()), request.getSourceIdentifier(), request.getSourceType(), request.getTransactionType(), customerCareContact});

        log.info("Otp message built for cardless cash transactions is : {}", message);
        var smsParams = new TwoFaSmsParams();
        smsParams.setMessage(message);
        return new TwoFaAdviceResponse(TwoFaType.SMS, smsParams);
    }

    private String getCustomerCareNumber(final RequestMetaData requestMetaData) {
        var segment = digitalUserSegmentService.getDigitalUserSegmentByName(requestMetaData.getSegment());
        var customerCareContact = segment.getLocalContactNumber();
        log.info("CustomerCare contact for the user {} with segment {} is {}", htmlEscape(requestMetaData.getUsername()),
                requestMetaData.getSegment(), htmlEscape(customerCareContact));
        return StringUtils.isEmpty(customerCareContact) ? "" : customerCareContact;
    }

    private RequestMetaData constructRequestMetaData() {

        String primaryCif = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-CIF");
        String channel = twoFaRequestHelper.getRequestHeaderValue("X-MOB-CHANNEL-NAME");
        String countryCode = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-COUNTRY");
        String loginId = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-LOGIN-ID");
        String userCacheKey = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-REDIS-KEY");
        String segment = twoFaRequestHelper.getRequestHeaderValue("X-USSM-SEGMENT");
        String userType = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-TYPE");
        String mobileNumber = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-MOBILE-NUMBER");
        String email = twoFaRequestHelper.getRequestHeaderValue("X-USSM-EMAILID");
        String username = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-NAME");
        String coRelationId = twoFaRequestHelper.getRequestHeaderValue("X-CORRELATION-ID");
        String suspendedUser = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-SUSPENDED-TXS");
        String deviceIP = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-DEVICE-IP");
        String region = twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-REGION");
        String language = twoFaRequestHelper.getRequestHeaderValue("X-MOB-LANGUAGE");
        String originClient = twoFaRequestHelper.getRequestHeaderValue("X-ORIGIN-CLIENT");
        String digitalUserId = twoFaRequestHelper.getRequestHeaderValue("x-ussm-user-iam-id");
        String tasTransactionId = twoFaRequestHelper.getRequestHeaderValue("x-tas-txn-id");
        String txnAction = twoFaRequestHelper.getRequestHeaderValue("x-txn-action");
        return RequestMetaData.builder()
                .primaryCif(primaryCif)
                .channel(channel)
                .country(countryCode)
                .loginId(loginId)
                .userCacheKey(userCacheKey)
                .segment(segment)
                .userType(userType)
                .mobileNUmber(mobileNumber)
                .email(email)
                .coRelationId(coRelationId)
                .username(username)
                .suspendedUser(suspendedUser)
                .deviceIP(deviceIP)
                .region(region)
                .language(language)
                .originClient(originClient)
                .digitalUserId(digitalUserId)
                .tasTransactionId(tasTransactionId)
                .txnAction(txnAction)
                .build();
    }
}
