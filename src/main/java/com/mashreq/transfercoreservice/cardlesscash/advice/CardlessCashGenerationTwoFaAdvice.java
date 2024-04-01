package com.mashreq.transfercoreservice.cardlesscash.advice;

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequestV2;
import com.mashreq.transfercoreservice.cardlesscash.service.DigitalUserSegmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mashreq.twofa.advice.TwoFaAdvisor;
import org.mashreq.twofa.advice.response.SmsParams;
import org.mashreq.twofa.advice.response.TwoFaAdvice;
import org.mashreq.twofa.core.aspect.TwoFaContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;

@Slf4j
@RequiredArgsConstructor
@Component
public class CardlessCashGenerationTwoFaAdvice implements TwoFaAdvisor {

    @Value("${twofa.otp.cardless-cash.msg}")
    private String cardlessCashTxnOtpMsg;

    private final DigitalUserSegmentService digitalUserSegmentService;

    @Override
    public boolean isTwoFaRequired(TwoFaContext twoFaContext) {
        return true;
    }

    @Override
    public TwoFaAdvice overrideAdvice(TwoFaContext twoFaContext) {
        var requestHeaderData = twoFaContext.getHeaders();
        var username = requestHeaderData.get("X-USSM-USER-NAME");
        var segment = requestHeaderData.get("X-USSM-SEGMENT");

        var request = (CardLessCashGenerationRequestV2)twoFaContext.getRequestBody();
        var customerCareContact = getCustomerCareNumber(username, segment);

        var message = StringUtils.replaceEach(cardlessCashTxnOtpMsg,
                new String[]{"{currency}", "{amount}", "{masked_account_no}", "{paymnt_source}", "{txn_type}"
                        , "{callcenter_contact}"},
                new String[]{request.getCurrencyCode(), String.valueOf(request.getAmount()), request.getSourceIdentifier(),
                        request.getSourceType(), request.getTransactionType(), customerCareContact});

        log.info("Otp message built for cardless cash transactions is : {}", message);
        var smsParams = SmsParams.builder()
                .message(message)
                .build();
        return TwoFaAdvice.withSMSParams(smsParams);
    }

    private String getCustomerCareNumber(String username, String headerSegment) {
        var segment = digitalUserSegmentService.getDigitalUserSegmentByName(headerSegment);
        var customerCareContact = segment.getLocalContactNumber();
        log.info("CustomerCare contact for the user {} with segment {} is {}", htmlEscape(username),
                headerSegment, htmlEscape(customerCareContact));
        return StringUtils.isEmpty(customerCareContact) ? "" : customerCareContact;
    }
}
