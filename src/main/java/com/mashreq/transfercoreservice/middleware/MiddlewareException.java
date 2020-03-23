package com.mashreq.transfercoreservice.middleware;

import lombok.Data;

@Data
public class MiddlewareException extends RuntimeException {
	private String errorCode;
	private String msg;
    public MiddlewareException(String errorCode, String message) {
			super();
			this.errorCode = errorCode;
			this.msg = message;
		}
}
