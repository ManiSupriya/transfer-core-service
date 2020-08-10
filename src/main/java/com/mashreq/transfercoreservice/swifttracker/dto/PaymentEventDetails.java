package com.mashreq.transfercoreservice.swifttracker.dto;
import lombok.Builder;
/**
 * @author SURESH PASUPULETI
 */
import lombok.Data;

@Data
@Builder
public class PaymentEventDetails {
	private String networkReference;
	private String msgNameId;
	private String businessService;
	private boolean valid; 
	private String instructionId;
	private TransactionStatus transactionStatus;
	private String from;
	private String to;
	private String originator;
	private String senderAckReceipt;
	private String ConfirmedAmount;
	private String confirmedAmountCcy;
	private ForeignExchangeDetails foreignExchangeDetails;
	private String updatePayment;
	private String duplicateMsgRef;
	private String CopiedBusinessService;
	private String lastUpdateTime;
	

}
