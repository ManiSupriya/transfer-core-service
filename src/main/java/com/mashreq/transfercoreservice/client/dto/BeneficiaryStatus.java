package com.mashreq.transfercoreservice.client.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author shahbazkh
 */

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BeneficiaryStatus {
    DRAFT("draft"),
    ACTIVE("active"),
    IN_COOLING_PERIOD("in_cooling"),
    DELETED("deleted"),
    SUSPENDED("suspened");

    private final String value;
}
