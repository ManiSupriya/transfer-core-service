package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.common.LocalIbanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_TYPE_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankDetailsResolverFactory {

    public static final String IBAN = "iban";
    public static final String ACCOUNT = "account";
    private final IbanBasedBankDetailsResolver ibanBasedBankDetailsResolver;
    private final AccountBasedBankDetailsResolver accountBasedBankDetailsResolver;
   private final Map<String,BankDetailsResolver> bankDetailsResolver = new HashMap<>();

   @PostConstruct
   public void init() {
       bankDetailsResolver.put(IBAN, ibanBasedBankDetailsResolver);
       bankDetailsResolver.put(ACCOUNT, accountBasedBankDetailsResolver);
   }

   public BankDetailsResolver getBankDetailsResolver(String type) {
       if (bankDetailsResolver.containsKey(type)) {
           return bankDetailsResolver.get(type);
       }
       GenericExceptionHandler.handleError(INVALID_TYPE_KEY, INVALID_TYPE_KEY.getErrorMessage());
        return null;
   }

}
