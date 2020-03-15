package com.mashreq.transfercoreservice.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

//TODO: MUST define what to allow null or not
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_history",
        indexes = @Index(columnList = "id", name = "cif_hidx"))
public class PaymentHistory extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String cif;

    private Long userId;
    private Long beneficiaryId;

    private String beneficiaryTypeCode;

    private String accountTo;
    @Column
    private String toCurrency;
    @Column
    private BigDecimal paidAmount;

    private String accountFrom;
    private String encryptedCardFrom;
    private String encryptedCardFromFourdigit;
    private String fromCurrency;

    private String billRefNo;
    private BigDecimal dueAmount;

    private String notes;
    private String mwReferenceNo;
    private String mwResponseDescription;
    private String mwResponseCode;
    private String channel;

    @Column
    private String status;

    private String ipAddress;
}
