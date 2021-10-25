package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "for transaction charges")
@Getter
@Setter
public class TransactionChargesDto {
	@ApiModelProperty(notes = "the account class / type in which the charge is applicable")
	private String accountClass;
	@ApiModelProperty(notes = "transaction fee for local transactions")
	private Double localTransactionCharge;
	@ApiModelProperty(notes = "transaction fee for international transactions")
	private Double internationalTransactionalCharge;
	@ApiModelProperty(notes = "transaction fee from other banks if the transaction goes through other banks for fulfilment")
	private Double coreBankTransactionFee;
}
