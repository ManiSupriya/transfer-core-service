package com.mashreq.transfercoreservice.swifttracker.dto;
import lombok.Builder;
/**
 * @author SURESH PASUPULETI
 */
import lombok.Data;

@Data
@Builder
public class TransactionStatus {
	private String status;
	private String reason;

}
