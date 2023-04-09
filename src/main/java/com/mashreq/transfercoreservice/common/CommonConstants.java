package com.mashreq.transfercoreservice.common;
/**
 * 
 * @author SURESH
 *
 */
public interface CommonConstants {
	String SEND_EMPTY_ERROR_RESPONSE = "SEND_EMPTY_RESPONSE";
	String EXCEEDS_WITHDRAWL_FREEQUENCY = "EXCEEDS_WITHDRAWL_FREEQUENCY";
	String EXCEEDS_WITHDRAWL_LIMIT = "EXCEEDS_WITHDRAWL_LIMIT";
	String MOB_CHANNEL = "MOBILE";
	String CARD_LESS_CASH = "cardless-cash";
	String NPSS = "NPSS";
	String SMILE_CARD_LOYALTY = "loyalty-simle-card";
	String SWIFT_GPI_TRANSACTION_DETAILS = "SWIFT-GPI-TRANSACTION-DETAILS";
	String VERSION_ID = "DEF-1";
	String CHANNEL_TRACE_ID = "X-CHANNEL-TRACE-ID";
	String X_REQUEST_METADATA = "X-REQUEST-METADATA";
	String INVALID_SESSION_TOKEN = "INVALID_SESSION_TOKEN";
	String OTP_VERIFIVATION_FAILED = "OTP_VERIFIVATION_FAILED";
	String MAX_OTP_ATTEMPTS_EXCEEDED = "MAX_OTP_ATTEMPTS_EXCEEDED";
	String USER_SESSION_ALREADY_INVALIDATED = "USER_SESSION_ALREADY_INVALIDATED";
	String MAX_OTP_FAILED_ATTEMPTS_EXCEEDED = "MAX_OTP_FAILED_ATTEMPTS_EXCEEDED";
	String FAILED_TO_VERIFY_OTP = "FAILED_TO_VERIFY_OTP";
	String DENIED_BY_POLICY_OTP_REUSE_NOT_ALLOWED = "DENIED_BY_POLICY_OTP_REUSE_NOT_ALLOWED";
	String OBJ_TOKENSTORE_ID_NOT_FOUND = "OBJ_TOKENSTORE_ID_NOT_FOUND";
	String NOT_FOUND_USER_IN_DB = "NOT_FOUND_USER_IN_DB";
	String USER_BLOCKED_STATUS = "USER_BLOCKED_STATUS";
	String USER_INACTIVE_STATUS = "USER_INACTIVE_STATUS";
	String FAILED_TO_DECRYPT = "FAILED_TO_DECRYPT";
	String LIMIT_CHECK_FAILED = "CARDLESS-CASH limit check failed ";
	String FUND_TRANSFER = "Fund Transfer";
	String ESB_SRV_FUND_TRANSFER = "fundTransfer";
	String SEARCH_ACCOUNT_DETAILS = "searchAccountDetails";
	String DEAL_VALIDATION = "DEAL_VALIDATION";
	String INVALID_DEAL_NUMBER = "RECORD_NOT_FOUND";
	String BASE_PACKAGE = "com.mashreq.transfercoreservice";
	String TITLE = "Transfer Core Service";
	String MS_SQUAD = "MS-Squad";
	String MAIL = "MicroservicesSquad@mashreq.com";
	String LICENCE_URL = "http://www.apache.org/licenses/LICENSE-2.0.html";
	String VERSION = "1.0.0";
	String LICENCE_TEXT = "Apache 2.0";
	String PROD_PROFILE = "prod";
}
