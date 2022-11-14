package com.mashreq.transfercoreservice.fundtransfer.dto;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
public class NpssEnrolmentStatusResponseDTO {
    private boolean askForEnrolment;
}
