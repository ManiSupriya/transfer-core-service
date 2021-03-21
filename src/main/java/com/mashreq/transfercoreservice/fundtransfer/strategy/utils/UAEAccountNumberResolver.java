package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mashreq.transfercoreservice.common.HtmlEscapeCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UAEAccountNumberResolver {
    private final Integer uaeIbanLength;
    private final Integer accountNumberLength;
    
    public UAEAccountNumberResolver(@Value("${app.uae.iban.length}") Integer uaeIbanLength, @Value("${app.uae.iban.accountNumber}") Integer accountNumberLength) {
		this.accountNumberLength = accountNumberLength;
		this.uaeIbanLength = uaeIbanLength;
    	
    }
    
    public String generateAccountNumber(String acountIdentifier) {
    	log.info("generating account number for {}",HtmlEscapeCache.htmlEscape(acountIdentifier));
    	if(uaeIbanLength.equals(acountIdentifier.length())){
    		/**returning last 12 digits*/
    		return acountIdentifier.substring(uaeIbanLength - accountNumberLength, uaeIbanLength);
    	}
		return acountIdentifier;
    }
    
}
