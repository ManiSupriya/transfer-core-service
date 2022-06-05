package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mashreq.transfercoreservice.common.HtmlEscapeCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountNumberResolver {
    private final Integer uaeIbanLength;
    private final Integer accountNumberLength;
    
    public AccountNumberResolver( @Value("${app.local.iban.length}") Integer uaeIbanLength,
								  @Value("${app.local.iban.accountNumber}") Integer accountNumberLength) {
		this.accountNumberLength = accountNumberLength;
		this.uaeIbanLength = uaeIbanLength;
    	
    }
    
    public String generateAccountNumber(String acountIdentifier) {
    	log.info("generating account number for {}",HtmlEscapeCache.htmlEscape(acountIdentifier));
    	if(uaeIbanLength.equals(acountIdentifier.length())){
    		/**returning last xx digits*/
    		return acountIdentifier.substring(uaeIbanLength - accountNumberLength, uaeIbanLength);
    	}
		return acountIdentifier;
    }
    
}
