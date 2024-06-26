package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TransactionChargesDto {
	private String accountClass;
	private Double localTransactionCharge;
	private Double internationalTransactionalCharge;
	private Double coreBankTransactionFee;
}
