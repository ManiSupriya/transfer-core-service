package com.mashreq.transfercoreservice.swifttracker.dto;
import lombok.Builder;
/**
 * @author SURESH PASUPULETI
 */
import lombok.Data;

@Data
@Builder
public class GPITransactionsDetailsRes {

	private String cifId;
	private String transactionRefNo;
	private String uetr;
	private String ultBeneficiary1;
	private String ultBeneficiary2;
	private String debitAccount;
	private String debitAccountBranch;
	private String debitAccountCcy;
	private String debitAmount;
	private String creditAccount;
	private String creditAccountBranch;
	private String creditAccountCcy;
	private String creditAmount;
	private String date;
	
}
