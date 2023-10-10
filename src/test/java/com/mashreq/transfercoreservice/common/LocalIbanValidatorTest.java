package com.mashreq.transfercoreservice.common;

import com.mashreq.ms.exceptions.GenericException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalIbanValidatorTest {
    private LocalIbanValidator localIbanValidator;

    @Test
    void testValidUAEIban() {
        localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
        String bankCode = localIbanValidator.validate("AE280330000010698008304");
        assertEquals("033", bankCode);
    }

    @Test
    void testValidEgyptIban() {
        localIbanValidator = new LocalIbanValidator("EG", "0046", 29, 12);
        String bankCode = localIbanValidator.validate("EG690046000400000059040009945");
        assertEquals("0046", bankCode);
    }

    //@Test
    void testNonValidUAEIbanChecksum() {
        localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
        try {
            localIbanValidator.validate("AE280330000050698008304");
            fail();
        } catch (GenericException ex) {
            assertEquals("TN-8027", ex.getErrorCode());
        }
    }

    //@Test
    void testNonValidEgyptIbanChecksum() {
        localIbanValidator = new LocalIbanValidator("EG", "0046", 29, 12);
        try {
            localIbanValidator.validate("EG690046000400000059040009947");
            fail();
        } catch (GenericException ex) {
            assertEquals("TN-8027", ex.getErrorCode());
        }
    }

    @Test
    void testNonValidUAEIbanLength() {
        localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
        try {
            localIbanValidator.validate("AE2803300000506980084");
            fail();
        } catch (GenericException ex) {
            assertEquals("TN-8028", ex.getErrorCode());
        }
    }

    @Test
    void testExtractAccountNumberIfMashreqIban() {
        localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
        String accountNumber = localIbanValidator.extractAccountNumberIfMashreqIban("AE280330000010698008304", "033");
        assertEquals("010698008304", accountNumber);
    }

    @Test
    void testExtractAccountNumberNonMashreq() {
        localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
        String accountNumber = localIbanValidator.extractAccountNumberIfMashreqIban("AE280370000010698008304", "037");
        assertNull(accountNumber);
    }

    @Test
    void testExtractAccountNumberNullIban() {
        localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
        String accountNumber = localIbanValidator.extractAccountNumberIfMashreqIban(null, "033");
        assertNull(accountNumber);
    }

    @Test
    void testLocalIban() {
        localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
        assertTrue(localIbanValidator.isLocalIban("AE280330000010698008304"));
        assertFalse(localIbanValidator.isLocalIban("EG690046000400000059040009945"));
    }
}