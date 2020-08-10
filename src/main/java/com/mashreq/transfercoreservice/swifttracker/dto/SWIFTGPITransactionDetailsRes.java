package com.mashreq.transfercoreservice.swifttracker.dto;
/**
 * @author SURESH PASUPULETI
 */
import java.util.List;

import com.mashreq.esbcore.bindings.account.mbcdm.PaymentEventDetailsType;
import com.mashreq.esbcore.bindings.account.mbcdm.TransactionStatusType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SWIFTGPITransactionDetailsRes {
	private String uetr;
	private TransactionStatusType transactionStatus;
	private String initiationTime;
	private String lastUpdateTime;
	private List<PaymentEventDetailsType> paymentEventDetails;

}
