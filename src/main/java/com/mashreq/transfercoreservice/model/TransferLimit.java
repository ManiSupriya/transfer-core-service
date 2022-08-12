package com.mashreq.transfercoreservice.model;

import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.TransferType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
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

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate = Instant.now();
}