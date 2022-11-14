package com.mashreq.transfercoreservice.fundtransfer.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class NpssEnrolmentStatusResponseDTO {
    private boolean askForEnrolment;
}
