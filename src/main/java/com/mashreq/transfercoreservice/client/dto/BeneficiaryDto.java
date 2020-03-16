package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author shahbazkh
 * @date 3/16/20
 */
@Data
public class BeneficiaryDto {
    private Long id;
    private String accountNumber;
    private String nickname;
    private String category;
    private String serviceType;
    private String serviceTypeCode;
    private BeneficiaryStatus status;
    private Instant createdDate;
    private Instant activeAfter;
    private String activeAfterDuration;

    // bill specific
    private String salikPinCode;
    private String creditcardHolderName;

    // Fields for beneficiary enquiry
    private String billRefNo;
    private BigDecimal dueAmount;
    private String outstandingAmount;
    private String currency;
}
