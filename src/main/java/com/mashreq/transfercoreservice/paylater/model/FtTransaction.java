package com.mashreq.transfercoreservice.paylater.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.paylater.enums.TransactionStatus;

//@Entity
//@Table(name = "ft_transaction")
public class FtTransaction extends DomainEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2909051579053564241L;
	@Column(name = "txn_id", nullable = false)
	private String txnId;
	@Column(name = "order_id")
	private String orderId;
	@Column(name = "exec_id")
	private String execId;
	@Column(name = "payment_id")
	private String paymentId;
	@Column(name = "transaction_date")
	private LocalDateTime transactionDate;
	@Embedded
	private Money transactionValue;
	@Column(name = "dest_ccy")
	private String destinationccy;
	@Column(name = "debit_amount")
	private BigDecimal debitAmount;
	@Column(name = "fx_exchange_rate")
	private BigDecimal fxExchangeRate;
	@Column(name = "core_ref")
	private String coreRef;
	@Column(name = "payment_msg_id")
	private String paymentMsgId;
	@Column(name = "error_code")
	private String errorCode;
	@Column(name = "error_message")
	private String errorMessage;
	@Column(name = "submission_req_time")
	private LocalDateTime submissionReqTime;
	@Column(name = "submission_resp_time")
	private LocalDateTime submissionRespTime;
	@Column(name = "sndr_mob")
	private String sndrMob;
	@Column(name = "sndr_email")
	private String sndrEmail;
	@Column(name = "sms_sent")
	private boolean smsSent;
	@Column(name = "email_sent")
	private boolean emailSent;
	@Column(name = "aggrgtr_ref")
	private String aggrgtrRef;
	@Column(name = "product_code")
	private String productCode;
	@Enumerated(EnumType.STRING)
	private TransactionStatus txnStatus;
	@Column(name = "sms_retry_count")
	private int smsRetryCount;
	@Column(name = "email_retry_count")
	private int emailRetryCount;
	@Column(name = "retry_count")
	private int retryCount;
	@Column(name = "retryable")
	private boolean retryable;
	@Column(name = "promo_code")
	private String promocode;
	@Column(name = "fx_exchange_rate")
	private String promocode_id;
	@Enumerated(EnumType.STRING)
	@Column(name = "service_type")
	private ServiceType serviceType;
}
