package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mashreq.transfercoreservice.common.HtmlEscapeCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountNumberResolver {
    private final int ibanLength;
    private final int accountNumberLength;
    
    public AccountNumberResolver( @Value("${app.local.iban.length}") Integer ibanLength,
								  @Value("${app.local.iban.accountNumber}") Integer accountNumberLength) {
		this.accountNumberLength = accountNumberLength;
		this.ibanLength = ibanLength;
    	
    }
    
    public String generateAccountNumber(String acountIdentifier) {
    	log.info("generating account number for {}",HtmlEscapeCache.htmlEscape(acountIdentifier));
    	if(ibanLength == acountIdentifier.length()){
    		/**returning last xx digits*/
    		return acountIdentifier.substring(ibanLength - accountNumberLength, ibanLength);
    	}
		return acountIdentifier;
    }
    
}
