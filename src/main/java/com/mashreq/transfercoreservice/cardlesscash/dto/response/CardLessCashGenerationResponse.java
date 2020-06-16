package com.mashreq.transfercoreservice.cardlesscash.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * This class is to hold the detail for the CLC Generated response.
 */
@Data
@Builder
public class CardLessCashGenerationResponse {

    private LocalDateTime expiryDateTime;
}
