package com.mashreq.transfercoreservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientDTO {
    private String requestedAmount;
    private String aed;
    private String value;
    private String contactName;
    private String proxy;
    private String receiverName;
    
}