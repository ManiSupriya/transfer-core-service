package com.mashreq.transfercoreservice.model;

import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.TransferType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfer_limit")
@ToString
@Getter
@Setter
@NamedNativeQuery(name = "TransferLimit.findTransactionCountAndTotalAmountBetweenDates",
        query = "SELECT count(id) as transfers, SUM(amount) as amount from transfer_limit limit where limit" +
                ".beneficiary_id=:beneficiaryId and limit.created_date between :fromDate and :toDate",
        resultSetMapping = "Mapping.TransferDetails")
@SqlResultSetMapping(name = "Mapping.TransferDetails",
        classes = @ConstructorResult(targetClass = TransferDetails.class,
                columns = {@ColumnResult(name = "transfers", type = Long.class),
                        @ColumnResult(name = "amount", type = BigDecimal.class)}))
public class TransferLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long beneficiaryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 2)
    private FTOrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type")
    private TransferType transferType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String transactionRefNo;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate = Instant.now();
}