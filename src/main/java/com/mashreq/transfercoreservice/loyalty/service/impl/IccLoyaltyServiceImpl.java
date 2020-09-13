package com.mashreq.transfercoreservice.loyalty.service.impl;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mashreq.transfercoreservice.loyalty.dto.IccLoyaltydto;
import com.mashreq.transfercoreservice.loyalty.service.IccLoyaltyService;
import com.mashreq.transfercoreservice.repository.IccLoyaltyRepository;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class IccLoyaltyServiceImpl implements IccLoyaltyService{
	private final IccLoyaltyRepository iccLoyaltyRepository;
	@Override
	public Response generateRedeemID(String cifId, String userCacheKey) {
		String sessionId = getMd5(cifId+LocalDateTime.now().toString());
		/*log.info("generting loyalty redeem ID for cif {} ", htmlEscape(sessionId));
		IccLoyaltydto.builder().cif(cifId).userSessionId(userCacheKey)
		.sessionId(sessionId).createdTime(LocalDateTime.now()).build();
		iccLoyaltyRepository.save(IccLoyaltydto.builder().cif(cifId).userSessionId(userCacheKey)
				.sessionId(sessionId).createdTime(LocalDateTime.now()).build());*/
		return Response.builder().status(ResponseStatus.SUCCESS).data(sessionId).build();
	}

	@Override
	public Response validateRedeemID(String sessionID) {
		return Response.builder().status(ResponseStatus.SUCCESS).build();
	}

	
	public static String getMd5(String input) 
    { 
        try { 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(input.getBytes()); 
            BigInteger no = new BigInteger(1, messageDigest); 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        } 
    } 

}
