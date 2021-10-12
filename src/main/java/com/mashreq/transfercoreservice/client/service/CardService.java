package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.MobRedisService;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.CardClient;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardSearchRequestDto;
import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.dto.CoreCardDetailsDto;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CARDS_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardClient cardClient;
    private final UserSessionCacheService userSessionCacheService;
    private final MobRedisService mobRedisService;

    public List<CardDetailsDTO> getCardsFromCore(final String cifId, CardType cardType) {
        log.info("Fetching cards for cifId {} and cardType {} ", htmlEscape(cifId), htmlEscape(getCardType(cardType)));
        try {
            Response<List<CoreCardDetailsDto>> cardsResponse = cardClient.getCards(cifId, getCardType(cardType));

            if (isNotBlank(cardsResponse.getErrorCode())) {
                log.warn("Not able to fetch cards, returning empty list instead");
                return Collections.emptyList();
            }

            List<CardDetailsDTO> cards = convertResponseToCards(cardsResponse.getData());
            log.info("{} Cards fetch for cif id {} and card type {} ", htmlEscape(Integer.toString(cards.size())), htmlEscape(cifId), htmlEscape(cardType));
            return cards;

        } catch (Exception e) {
            log.error("Error occurred while calling card client {} ", e);
            return Collections.emptyList();
        }
    }

    private CardDetailsDTO getCardDetailsFromCore(final String cardNumber) {
        log.info("[CardService] Fetching card details");

        Response<CoreCardDetailsDto> response = cardClient.getCardDetails(CardSearchRequestDto.builder().cardNumber(cardNumber).build());

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            log.warn("Not able to fetch cards");
            GenericExceptionHandler.handleError(CARDS_EXTERNAL_SERVICE_ERROR,
                    CARDS_EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(response));
        }
        CardDetailsDTO card = convertCoreCardToCard(response.getData());
        log.info("[CardService] Cards fetched successfully");
        return card;
    }

    private List<CardDetailsDTO> convertResponseToCards(List<CoreCardDetailsDto> coreCardsDTOs) {
        return coreCardsDTOs.stream()
                .map(coreCard -> convertCoreCardToCard(coreCard))
                .collect(Collectors.toList());
    }

    private CardDetailsDTO convertCoreCardToCard(CoreCardDetailsDto coreCard) {
    	CardDetailsDTO cardDetailsDTO = new CardDetailsDTO();
    	cardDetailsDTO.setAvailableCreditLimit(coreCard.getAvailableCreditLimit());
    	cardDetailsDTO.setCardHolderName(coreCard.getCardHolderName());
    	cardDetailsDTO.setCardNo(coreCard.getCardNo());
    	cardDetailsDTO.setCardType(coreCard.getCardType());
    	cardDetailsDTO.setCardStatus(coreCard.getCardStatus());
    	cardDetailsDTO.setSegment("conventional");
    	cardDetailsDTO.setEncryptedCardNumber(coreCard.getEncryptedCardNumber());
    	cardDetailsDTO.setExpiryDate(coreCard.getExpiryDate());
    	cardDetailsDTO.setCardAccountNumber(coreCard.getCardAccountNumber());
        return cardDetailsDTO;
    }

    private CardType getCardType(final CardType cardType) {
        return cardType == null ? CardType.CC : cardType;
    }

    public CardDetailsDTO getCardDetailsFromCache(final String cardNumber, RequestMetaData requestMetaData) {
        userSessionCacheService.isCardNumberBelongsToCif(cardNumber, requestMetaData.getUserCacheKey());
        CardDetailsDTO cardDetailsDTO = mobRedisService.get(userSessionCacheService.getCardDetailsCacheKey(requestMetaData, cardNumber), CardDetailsDTO.class);
        if(cardDetailsDTO == null){
            cardDetailsDTO = getCardDetailsFromCore(cardNumber);
        }
        return cardDetailsDTO;
    }

}
