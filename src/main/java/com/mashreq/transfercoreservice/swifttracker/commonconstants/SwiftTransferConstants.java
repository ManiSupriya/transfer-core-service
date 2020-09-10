package com.mashreq.transfercoreservice.swifttracker.commonconstants;

public interface SwiftTransferConstants {
	String CRDT_STATUS = "ACSC";
    String RJCT_STATUS="RJCT";
    String PNDG_STATUS="ACSP";
    String TRNSCTN="Transaction ";
    String TRNSCTN_PNDG="Transaction Pending: ";
    String REASON_G000 = "G000";
    String REASON_G001 = "G001";
    String REASON_G002 = "G002";
    String REASON_G003 = "G003";
    String REASON_G004 = "G004";
    String CREDITED = "credited";
    String REJECTED = "rejected";
    String REASON_G000_MESSAGE = "Onward payment sent";
    String REASON_G001_MESSAGE = "No longer traceable";
    String REASON_G002_MESSAGE = "Cannot credit same day";
    String REASON_G003_MESSAGE = "Pending documents";
    String REASON_G004_MESSAGE = "Waiting for funds";
    String DAY = "d ";
    String HOUR = "h ";
    String MIN = "m ";
    String EMPTY = "";
    String GPI_TRACKER_URL =  "/v1/gpiTransaction";
    String SWIFT_TRACKER = "/swiftTransfer";
    String REQ_METADATA = "X-REQUEST-METADATA";
    String GPI_TRANSACTIONS = "GPI Transactions";
}
