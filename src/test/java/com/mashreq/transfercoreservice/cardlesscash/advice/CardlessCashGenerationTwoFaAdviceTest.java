package com.mashreq.transfercoreservice.cardlesscash.advice;

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequestV2;
import com.mashreq.transfercoreservice.cardlesscash.service.DigitalUserSegmentService;
import com.mashreq.transfercoreservice.model.Segment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mashreq.twofa.core.aspect.TwoFaContext;
import org.mashreq.twofa.core.types.TwoFaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CardlessCashGenerationTwoFaAdviceTest {

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
        var twofaContext = TwoFaContext.builder()
                .requestBody(cardLessCashGenerationRequestV2)
                .build();
        assertTrue(cardlessCashGenerationTwoFaAdvice.isTwoFaRequired(twofaContext));
    }

    @Test
    public void testOverrideAdvice(){

        var accountNumber = "019100064328";
        var amount = new BigDecimal("1000");

        var cardLessCashMsg = "is OTP for {txn_type} from {paymnt_source} ending {masked_account_no} for {currency} {amount}. " +
                "Call us at {callcenter_contact} if you haven't initiated this";
        ReflectionTestUtils.setField(cardlessCashGenerationTwoFaAdvice, "cardlessCashTxnOtpMsg", cardLessCashMsg);

        var cardLessCashGenerationRequestV2 = new CardLessCashGenerationRequestV2();
        cardLessCashGenerationRequestV2.setAccountNo(accountNumber);
        cardLessCashGenerationRequestV2.setAmount(amount);
        cardLessCashGenerationRequestV2.setCurrencyCode("AED");
        cardLessCashGenerationRequestV2.setSourceIdentifier("12312");
        cardLessCashGenerationRequestV2.setSourceType("MOB");
        cardLessCashGenerationRequestV2.setTransactionType("Card Less Cash");
        cardLessCashGenerationRequestV2.setBeneficiaryName("Bene1");

        var segment = new Segment();
        segment.setName("GOLD");
        segment.setCustomerCareNumber("+971 123 4567");
        segment.setLocalContactNumber("+971 456 789");

        var requestHeaderMap = new HashMap<String, String>();
        requestHeaderMap.put("X-USSM-USER-CIF", "1213123");
        requestHeaderMap.put("X-MOB-CHANNEL-NAME", "MOBILE");
        requestHeaderMap.put("X-USSM-USER-COUNTRY", "AE");
        requestHeaderMap.put("X-USSM-USER-NAME", "cardlesscashuser");
        requestHeaderMap.put("X-USSM-SEGMENT", "GOLD");
        var twofaContext = TwoFaContext.builder()
                .headers(requestHeaderMap)
                .requestBody(cardLessCashGenerationRequestV2)
                .build();


        when(digitalUserSegmentService.getDigitalUserSegmentByName("GOLD")).thenReturn(segment);

        var response = cardlessCashGenerationTwoFaAdvice.overrideAdvice(twofaContext);

        assertNull(response.twoFaType());
        assertEquals("is OTP for Card Less Cash from MOB ending 12312 for AED 1000. " +
                "Call us at +971 456 789 if you haven't initiated this", response.smsParams().getMessage());
    }
}
