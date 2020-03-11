package com.mashreq.transfercoreservice.paymentoptions.utils;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;

import java.util.function.Predicate;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
public final class PaymentPredicates {

    private static final String CARD_ACTIVE = "ACTIVE";
    private static final String CARD_PRIMARY = "P";
    private static final String ACCOUNT_ACTIVE = "ACTIVE";
    private static final String ACCOUNT_DORMANT = "DORMANT";
    private static final String CURRENCY_AED = "AED";
    private static final String CURRENT_ACCOUNT = "CA";
    private static final String SAVINGS_ACCOUNT = "SA";

    public static final Predicate<CardDetailsDTO> billPaymentCardFilterSource() {
        return isActiveCard().and(isPrimaryCard());
    }

    public static final Predicate<AccountDetailsDTO> billPaymentAccountFilterSource() {
        return isCasaAccount().and(isActiveAccount()).and(isAEDCurrencyAccount());
    }

    public static final Predicate<AccountDetailsDTO> fundTransferAccountFilterSource() {
        return isCasaAccount().and(isActiveAccount());
    }

    public static final Predicate<AccountDetailsDTO> fundTransferOwnAccountFilterDestination() {
        return isCasaAccount().and(isActiveAccount().or(isDormantAccount()));
    }


    private static Predicate<CardDetailsDTO> isPrimaryCard() {
        return card -> CARD_PRIMARY.equals(card.getCardType());
    }

    private static Predicate<CardDetailsDTO> isActiveCard() {
        return card -> CARD_ACTIVE.equals(card.getCardStatus());
    }

    private static Predicate<AccountDetailsDTO> isAEDCurrencyAccount() {
        return account -> CURRENCY_AED.equals(account.getCurrency());
    }

    private static Predicate<AccountDetailsDTO> isDormantAccount() {
        return account -> ACCOUNT_DORMANT.equals(account.getStatus());
    }

    private static Predicate<AccountDetailsDTO> isActiveAccount() {
        return account -> ACCOUNT_ACTIVE.equals(account.getStatus());
    }

    private static Predicate<AccountDetailsDTO> isCasaAccount() {
        return account -> CURRENT_ACCOUNT.equals(account.getSchemeType()) || SAVINGS_ACCOUNT.equals(account.getSchemeType());
    }
}
