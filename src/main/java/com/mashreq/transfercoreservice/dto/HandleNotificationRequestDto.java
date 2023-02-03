package com.mashreq.transfercoreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by KrishnaKo on 18/01/2023
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandleNotificationRequestDto {

    private NotificationRequestDto notificationRequestDto;
    private TransactionHistoryDto transactionHistoryDto;
}
