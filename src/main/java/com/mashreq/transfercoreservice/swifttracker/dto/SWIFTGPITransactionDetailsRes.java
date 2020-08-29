package com.mashreq.transfercoreservice.swifttracker.dto;
/**
 * @author SURESH PASUPULETI
 */
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SWIFTGPITransactionDetailsRes {
	private String transactionStatus;
	private String initiationTime;
	private String lastUpdateTime;
	private String completionTime;
	private String totalProcessingTime;
	private List<PaymentEventDetailsType> paymentEventDetails;

}
