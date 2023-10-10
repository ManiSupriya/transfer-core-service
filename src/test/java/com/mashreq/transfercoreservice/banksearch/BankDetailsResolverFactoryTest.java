package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.ms.exceptions.GenericException;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BankDetailsResolverFactoryTest {

    @InjectMocks
    private BankDetailsResolverFactory bankDetailsResolverFactory;
    @Mock
    private IbanBasedBankDetailsResolver ibanBasedBankDetailsResolver;
    @Mock
    private AccountBasedBankDetailsResolver accountBasedBankDetailsResolver;

    @BeforeEach
    public void init() {
        final Map<String,BankDetailsResolver> bankDetailsResolver = new HashMap<>();
        bankDetailsResolver.put("iban", ibanBasedBankDetailsResolver);
        bankDetailsResolver.put("account", accountBasedBankDetailsResolver);
        ReflectionTestUtils.setField(bankDetailsResolverFactory, "bankDetailsResolver", bankDetailsResolver);

    }

    @Test
    public void test_getBankDetailsResolver_iban() {

        BankDetailsResolver iban = bankDetailsResolverFactory.getBankDetailsResolver("iban");
        assertEquals(ibanBasedBankDetailsResolver,iban);
    }

    @Test
    public void test_getBankDetailsResolver_account() {

        BankDetailsResolver iban = bankDetailsResolverFactory.getBankDetailsResolver("account");
        assertEquals(accountBasedBankDetailsResolver,iban);
    }

    @Test()
    public void test_getBankDetailsResolver_other() {
        assertThrows(GenericException.class, () ->bankDetailsResolverFactory.getBankDetailsResolver("other"));
    }


}
