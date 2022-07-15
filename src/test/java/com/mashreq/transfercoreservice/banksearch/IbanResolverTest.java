package com.mashreq.transfercoreservice.banksearch;


import com.mashreq.ms.exceptions.GenericException;
import org.apache.commons.validator.routines.IBANValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IbanResolverTest {

    @InjectMocks
    private IbanResolver ibanResolver;
    @Mock
    private IBANValidator ibanValidator;

    @Test(expected = GenericException.class)
    public void test_Iban_generation_failure_missing_bank_code() {
        //Given
        String account = "085465347574";
        String bankCode = "";
        String branchCode = "087";

        ibanResolver.constructIBAN(account,bankCode, branchCode);
    }

    @Test(expected = GenericException.class)
    public void test_Iban_generation_failure_missing_branch_code() {
        //Given
        String account = "085465347574";
        String bankCode = "097";
        String branchCode = "";

        ibanResolver.constructIBAN(account,bankCode, branchCode);
    }

    @Test(expected = GenericException.class)
    public void test_Iban_generation_failure_missing_account() {
        //Given
        String account = "";
        String bankCode = "097";
        String branchCode = "065";

        ibanResolver.constructIBAN(account,bankCode, branchCode);
    }



    @Test(expected = GenericException.class)
    public void test_Iban_generation_failure_non_numeric_bank_code() {
        //Given
        String account = "085465347574";
        String bankCode = "0FRG";
        String branchCode = "065";

        ibanResolver.constructIBAN(account,bankCode, branchCode);
    }

    @Test(expected = GenericException.class)
    public void test_Iban_generation_failure_non_numeric_branch_code() {
        //Given
        String account = "085465347574";
        String bankCode = "0987";
        String branchCode = "0FRG";

        ibanResolver.constructIBAN(account,bankCode, branchCode);
    }

    @Test(expected = GenericException.class)
    public void test_Iban_generation_failure_non_numeric_account() {
        //Given
        String account = "0854653S47574";
        String bankCode = "0987";
        String branchCode = "087";

        ibanResolver.constructIBAN(account,bankCode, branchCode);
    }

    @Test
    public void test_Iban_generation_success_without_prefix() {
        //Given
        String account = "000029991234567";
        String bankCode = "0001";
        String branchCode = "0036";

        String iban = ibanResolver.constructIBAN(account,bankCode, branchCode);
        Assert.assertEquals("EG110001003600000029991234567",iban);
    }

    @Test
    public void test_Iban_generation_success_with_prefix() {
        //Given
        String account = "046534757476543";
        String bankCode = "987";
        String branchCode = "65";

        String iban = ibanResolver.constructIBAN(account,bankCode, branchCode);
        Assert.assertEquals("EG220987006500046534757476543",iban);
    }
}
