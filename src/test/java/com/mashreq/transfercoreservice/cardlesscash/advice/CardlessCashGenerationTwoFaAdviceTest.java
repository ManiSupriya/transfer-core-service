package com.mashreq.transfercoreservice.cardlesscash.advice;

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequestV2;
import com.mashreq.transfercoreservice.cardlesscash.service.DigitalUserSegmentService;
import com.mashreq.transfercoreservice.model.Segment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mashreq.twofa.domain.TwoFaType;
import org.mashreq.twofa.utils.http.TwoFaRequestHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CardlessCashGenerationTwoFaAdviceTest {

    @Mock
    private TwoFaRequestHelper twoFaRequestHelper;

    @Mock
    private DigitalUserSegmentService digitalUserSegmentService;

    @InjectMocks
    private CardlessCashGenerationTwoFaAdvice cardlessCashGenerationTwoFaAdvice;

    @Test
    public void testIsTwoFaRequired(){

        var accountNumber = "019100064328";
        var amount = new BigDecimal("1000");

        ReflectionTestUtils.setField(cardlessCashGenerationTwoFaAdvice, "cardlessCashTxnOtpMsg", "OTP for Cardless Cash");

        var cardLessCashGenerationRequestV2 = CardLessCashGenerationRequestV2.builder()
                .accountNo(accountNumber)
                .amount(amount)
                .currencyCode("AED")
                .sourceIdentifier("12312")
                .sourceType("MOB")
                .transactionType("Card Less Cash")
                .build();

        assertTrue(cardlessCashGenerationTwoFaAdvice.isTwoFaRequired(cardLessCashGenerationRequestV2));
    }

    @Test
    public void testOverrideAdvice(){

        var accountNumber = "019100064328";
        var amount = new BigDecimal("1000");

        var cardLessCashMsg = "is OTP for {txn_type} from {paymnt_source} ending {masked_account_no} for {currency} {amount}. " +
                "Call us at {callcenter_contact} if you haven't initiated this";
        ReflectionTestUtils.setField(cardlessCashGenerationTwoFaAdvice, "cardlessCashTxnOtpMsg", cardLessCashMsg);

        var cardLessCashGenerationRequestV2 = CardLessCashGenerationRequestV2.builder()
                .accountNo(accountNumber)
                .amount(amount)
                .currencyCode("AED")
                .sourceIdentifier("12312")
                .sourceType("MOB")
                .transactionType("Card Less Cash")
                .build();

        var segment = new Segment();
        segment.setName("GOLD");
        segment.setCustomerCareNumber("+971 123 4567");
        segment.setLocalContactNumber("+971 456 789");

        when(twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-CIF")).thenReturn("1213123");
        when(twoFaRequestHelper.getRequestHeaderValue("X-MOB-CHANNEL-NAME")).thenReturn("MOBILE");
        when(twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-COUNTRY")).thenReturn("AE");
        when(twoFaRequestHelper.getRequestHeaderValue("X-USSM-USER-NAME")).thenReturn("cardlesscashuser");
        when(twoFaRequestHelper.getRequestHeaderValue("X-USSM-SEGMENT")).thenReturn("GOLD");
        when(digitalUserSegmentService.getDigitalUserSegmentByName("GOLD")).thenReturn(segment);

        var response = cardlessCashGenerationTwoFaAdvice.overrideAdvice(cardLessCashGenerationRequestV2);

        assertEquals(TwoFaType.SMS, response.twoFaType());
    }
}
