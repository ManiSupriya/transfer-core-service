package com.mashreq.transfercoreservice.cardlesscash.dto.response;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is to hold the detail for the CLC Generated response.
 */
@Data
@NoArgsConstructor
public class CardLessCashGenerationResponse {

    private LocalDateTime expiryDateTime;
    private String referenceNumber;
}
