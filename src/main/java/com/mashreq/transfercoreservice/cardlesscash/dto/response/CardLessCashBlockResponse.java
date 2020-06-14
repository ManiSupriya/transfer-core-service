package com.mashreq.transfercoreservice.cardlesscash.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * This class is to hold the detail for blocking the CLC response.
 */
@Builder
@Data
public class CardLessCashBlockResponse {

    private boolean success;
}
