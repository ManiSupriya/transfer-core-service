package com.mashreq.transfercoreservice.loyaltysmilecard.service.impl;

import static com.mashreq.transfercoreservice.common.CommonConstants.SMILE_CARD_LOYALTY;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.config.ICCLoyaltySmileCardConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.loyaltysmilecard.dto.IccLoyaltySmileCardGenResDTO;
import com.mashreq.transfercoreservice.loyaltysmilecard.dto.IccLoyaltySmileCardValResDTO;
import com.mashreq.transfercoreservice.loyaltysmilecard.dto.IccLoyaltySmileCarddto;
import com.mashreq.transfercoreservice.loyaltysmilecard.service.IccLoyaltySmileCardService;
import com.mashreq.transfercoreservice.repository.IccLoyaltySmileCardRepository;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class IccLoyaltySmileCardServiceImpl implements IccLoyaltySmileCardService{
	private final IccLoyaltySmileCardRepository iccLoyaltySmileCardRepository;
	private final ICCLoyaltySmileCardConfig iCCLoyaltySmileCardConfig;
	private final CardService cardService;
	private final AsyncUserEventPublisher asyncUserEventPublisher;
	private final EncryptionService encryptionService = new EncryptionService();
	private static final String SHA256_STRING = "SHA-256";
	private static final String RNG_ALGO = "SHA1PRNG";

	@Override
	public Response<Object> generateRedeemIDforSmileCard(String cifId, String userCacheKey) {
		String sessionId = hash(cifId+LocalDateTime.now().toString());
		log.info("generting loyalty redeem ID for cif {} ", htmlEscape(sessionId));
		IccLoyaltySmileCarddto iccLoyaltydto = new IccLoyaltySmileCarddto();
		iccLoyaltydto.setCif(cifId);
		iccLoyaltydto.setUserSessionId(userCacheKey);
		iccLoyaltydto.setSessionId(sessionId);
		iccLoyaltydto.setCreatedTime(LocalDateTime.now());
		iccLoyaltydto.setUpdatedTime(LocalDateTime.now());
		iccLoyaltySmileCardRepository.save(iccLoyaltydto);
		RequestMetaData metaData = new RequestMetaData();
		metaData.setPrimaryCif(cifId);
		metaData.setUserCacheKey(userCacheKey);
		asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.LOYALTY_SMILE_CARD_GEN_DETAILS, metaData, FundTransferEventType.LOYALTY_SMILE_CARD_GEN_DETAILS.getDescription());
		return Response.builder().status(ResponseStatus.SUCCESS).data(IccLoyaltySmileCardGenResDTO.builder().sessionId(sessionId).build()).build();
	}

	@Override
	public Response<Object> validateRedeemIDforSmileCard(String cifId, String sessionID) {

		RequestMetaData metaData = new RequestMetaData();
		metaData.setPrimaryCif(cifId);
		Optional<IccLoyaltySmileCarddto> iccLoyaltySession = iccLoyaltySmileCardRepository.findBySessionIdEquals(sessionID);
		if (!iccLoyaltySession.isPresent()) {
			log.info("SessionId is Invalid");
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.LOYALTY_SMILE_CARD_ERROR,
					metaData, SMILE_CARD_LOYALTY, metaData.getChannelTraceId(),
					TransferErrorCode.INVALID_SESSION_ID.toString(),
					TransferErrorCode.INVALID_SESSION_ID.getErrorMessage(),
					TransferErrorCode.INVALID_SESSION_ID.getErrorMessage());
			GenericExceptionHandler.handleError(INVALID_SESSION_ID, INVALID_SESSION_ID.getErrorMessage(), INVALID_SESSION_ID.getErrorMessage());
		}
		log.info("SessionId found successfully {} ", iccLoyaltySession.get());IccLoyaltySmileCarddto iccLoyaltydto = iccLoyaltySession.get();
		int minutes = (int) ChronoUnit.MINUTES.between(iccLoyaltydto.getCreatedTime(), LocalDateTime.now()); 
		log.info("time difference to validate session ID in minutes {}",minutes);
		if(minutes>iCCLoyaltySmileCardConfig.getTimeInterval()) {
			log.info("SessionId is Expired");
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.LOYALTY_SMILE_CARD_ERROR,
					metaData, SMILE_CARD_LOYALTY, metaData.getChannelTraceId(),
					TransferErrorCode.EXPIRED_SESSION_ID.toString(),
					TransferErrorCode.EXPIRED_SESSION_ID.getErrorMessage(),
					TransferErrorCode.EXPIRED_SESSION_ID.getErrorMessage());
			GenericExceptionHandler.handleError(EXPIRED_SESSION_ID, EXPIRED_SESSION_ID.getErrorMessage(), EXPIRED_SESSION_ID.getErrorMessage());	
		}
		 List<CardDetailsDTO> cards = cardService.getCardsFromCore(cifId, CardType.DC);
		 for(CardDetailsDTO currCardDetails : cards){
	            String decryptedCardNo = encryptionService.decrypt(currCardDetails.getEncryptedCardNumber());
	            if(decryptedCardNo.startsWith(iCCLoyaltySmileCardConfig.getSmilesProductBin())){
	        		asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.LOYALTY_SMILE_CARD_VAL_DETAILS, metaData, FundTransferEventType.LOYALTY_SMILE_CARD_VAL_DETAILS.getDescription());
	            	return Response.builder().status(ResponseStatus.SUCCESS).data(IccLoyaltySmileCardValResDTO.builder().cardAccountNumber(currCardDetails.getCardAccountNumber()).build()).build();
	            }
	        }
		 log.info("No matching card details found for PRODUCT BIN ID");
		 asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.LOYALTY_SMILE_CARD_ERROR,
					metaData, SMILE_CARD_LOYALTY, metaData.getChannelTraceId(),
					TransferErrorCode.NOT_MATCHING_CARD_DETAILS.toString(),
					TransferErrorCode.NOT_MATCHING_CARD_DETAILS.getErrorMessage(),
					TransferErrorCode.NOT_MATCHING_CARD_DETAILS.getErrorMessage());
		 GenericExceptionHandler.handleError(NOT_MATCHING_CARD_DETAILS,NOT_MATCHING_CARD_DETAILS.getErrorMessage(),NOT_MATCHING_CARD_DETAILS.getErrorMessage());
		return Response.builder().status(ResponseStatus.ERROR).build();
	}

	public static String hash(String input) {
		String generatedHashedValue = null;
		try {

			MessageDigest md = MessageDigest.getInstance(SHA256_STRING);
			md.update(getSalt());

			byte[] bytes = md.digest(input.getBytes());
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			generatedHashedValue = sb.toString();
		} catch (Exception e) {
			log.error("Exception while generating hash for Digital user CIF", e);
			GenericExceptionHandler.handleError(INTERNAL_ERROR, INTERNAL_ERROR.getErrorMessage());
		}
		return generatedHashedValue;
	}

	private static byte[] getSalt() throws NoSuchAlgorithmException {

		SecureRandom sr = SecureRandom.getInstance(RNG_ALGO);
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;

	}

}
