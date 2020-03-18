package com.mashreq.transfercoreservice.client.service;

import com.mashreq.transfercoreservice.client.CardClient;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.dto.CoreCardDetailsDto;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<CardDetailsDTO> getCardsFromCore(final String cifId, CardType cardType) {
        log.info("Fetching cards for cifId {} and cardType {} ", cifId, getCardType(cardType));
        try {
            Response<List<CoreCardDetailsDto>> cardsResponse = cardClient.getCards(cifId, getCardType(cardType));

            if (isNotBlank(cardsResponse.getErrorCode())) {
                log.warn("Not able to fetch cards, returning empty list instead");
                return Collections.emptyList();
            }

            List<CardDetailsDTO> cards = convertResponseToCards(cardsResponse.getData());
            log.info("{} Cards fetch for cif id {} and card type {} ", cards.size(), cifId, cardType);
            return cards;

        } catch (Exception e) {
            log.error("Error occurred while calling card client {} ", e);
            return Collections.emptyList();
        }
    }

    private List<CardDetailsDTO> convertResponseToCards(List<CoreCardDetailsDto> coreCardsDTOs) {
        return coreCardsDTOs.stream()
                .map(coreCard -> convertCoreCardToCard(coreCard))
                .collect(Collectors.toList());
    }

    private CardDetailsDTO convertCoreCardToCard(CoreCardDetailsDto coreCard) {
        return CardDetailsDTO.builder()
                .availableCreditLimit(coreCard.getAvailableCreditLimit())
                .cardHolderName(coreCard.getCardHolderName())
                .cardNo(coreCard.getCardNo())
                .cardType(coreCard.getCardType())
                .cardStatus(coreCard.getCardStatus())
                .segment("conventional")
                .encryptedCardNumber(coreCard.getEncryptedCardNumber())
                .build();
    }

    private CardType getCardType(final CardType cardType) {
        return cardType == null ? CardType.CC : cardType;
    }
}
