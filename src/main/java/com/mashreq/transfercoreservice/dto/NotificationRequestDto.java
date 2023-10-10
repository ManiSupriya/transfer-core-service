package com.mashreq.transfercoreservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by KrishnaKo on 24/11/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String limitVersionUuid;
	private BigDecimal amount;
	private String notificationType;
	/* Added For NPSS check and remove above as required */
	private String customerName;
	private String contactName;
	private String referenceNumber;
	private String sentTo;
	private String date;
	private String time;
	private String reasonForFailure;
	private String fromAccount;
	private String receiverName;
	private String proxy;
	private List<RtpNotification> rtpNotificationList;
	private String emailProxy;

}
