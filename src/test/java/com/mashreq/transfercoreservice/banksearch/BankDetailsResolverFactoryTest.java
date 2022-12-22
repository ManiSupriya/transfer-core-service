package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.ms.exceptions.GenericException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class BankDetailsResolverFactoryTest {

    @InjectMocks
    private BankDetailsResolverFactory bankDetailsResolverFactory;
    @Mock
    private IbanBasedBankDetailsResolver ibanBasedBankDetailsResolver;
    @Mock
    private AccountBasedBankDetailsResolver accountBasedBankDetailsResolver;

    @Before
    public void init() {
        final Map<String,BankDetailsResolver> bankDetailsResolver = new HashMap<>();
        bankDetailsResolver.put("iban", ibanBasedBankDetailsResolver);
        bankDetailsResolver.put("account", accountBasedBankDetailsResolver);
        ReflectionTestUtils.setField(bankDetailsResolverFactory, "bankDetailsResolver", bankDetailsResolver);

    }

    @Test
    public void test_getBankDetailsResolver_iban() {

        BankDetailsResolver iban = bankDetailsResolverFactory.getBankDetailsResolver("iban");
        Assert.assertEquals(ibanBasedBankDetailsResolver,iban);
    }

    @Test
    public void test_getBankDetailsResolver_account() {

        BankDetailsResolver iban = bankDetailsResolverFactory.getBankDetailsResolver("account");
        Assert.assertEquals(accountBasedBankDetailsResolver,iban);
    }

    @Test(expected = GenericException.class)
    public void test_getBankDetailsResolver_other() {

        BankDetailsResolver iban = bankDetailsResolverFactory.getBankDetailsResolver("other");

    }


}
