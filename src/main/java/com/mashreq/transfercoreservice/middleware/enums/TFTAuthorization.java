package com.mashreq.transfercoreservice.middleware.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TFTAuthorization {
	AUTHORIZED("A"),HOLD("H");
	private final String code;
}
