package com.mashreq.transfercoreservice.paylater.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.OrderStatus;
import com.mashreq.transfercoreservice.paylater.enums.SIFrequencyType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "fund_transfer_order")
public class FundTransferOrder extends DomainEntity {
	/**
	 * TODO: add account details for WYMA transfer
	 */
	private static final long serialVersionUID = 3208787880578091943L;
	@Column(name = "order_id",length = 15,nullable = false)
	private String orderId;
	@Enumerated(EnumType.STRING)
	@Column(name = "order_type", nullable = false,length = 2)
	private FTOrderType orderType;
	@Enumerated(EnumType.STRING)
	@Column(name = "frequency",length = 10)
	private SIFrequencyType frequency;
	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;
	@Column(name = "end_date")
	private LocalDateTime endDate;
	@Column(name = "next_execution_time")
	private LocalDateTime nextExecutionTime;
	@Embedded
	private Money transactionValue;
	@Column(name = "src_ccy", nullable = false,length = 3)
	private String sourceCurrency;
	@Column(name = "dest_ccy",length = 3)
	private String destinationAccountCurrency;
	@Column(name = "dest_acc_no",length = 40)
	private String destinationAccountNumber;
	@Column(name = "ben_ref")
	private Long beneficiaryId;
	@Column(name = "cif", nullable = false,length = 12)
	private String cif;
	@Column(name = "internal_acc_flag",length = 1)
	private String internalAccFlag;
	@Column(name = "purpose_code",length = 50)
	private String purposeCode;
	@Column(name = "purpose_desc")
	private String purposeDesc;
	@Column(name = "channel",length = 15)
	private String channel;
	@Enumerated(EnumType.STRING)
	@Column(name = "order_status", nullable = false, length = 15)
	private OrderStatus OrderStatus;
	@Column(name = "product_id",length = 20)
	private String productId;
	@Column(name = "user_type",length = 15)
	private String userType;
	@Column(name = "fx_deal_number",length = 20)
	private String fxDealNumber;
	@Column(name = "deal_rate",precision = 5)
	private BigDecimal dealRate;
	@Column(name = "charge_bearer",length = 1)
	@Enumerated(EnumType.STRING)
	private ChargeBearer chargeBearer;
	@Column(name = "transaction_Code",length = 10)
	private String transactionCode;
	@Enumerated(EnumType.STRING)
	@Column(name = "service_type",nullable = false,length = 10)
	private ServiceType serviceType;
	@Column(name = "financial_transaction_no",length = 100)
	private String financialTransactionNo;
	@Column(name = "customer_segment",length = 20)
	private String customerSegment;
	@Column(name = "source_branch_code",length = 20)
	private String sourceBranchCode;
	@Column(name = "payment_note")
	private String paymentNote;
	@Column(name = "src_account_number",length = 15)
	private String sourceAccount;
	@Column(name = "email",length = 150)
	private String email;
	@Column(name = "trx_ref_no",length = 20)
	private String trxRefNo;
}
