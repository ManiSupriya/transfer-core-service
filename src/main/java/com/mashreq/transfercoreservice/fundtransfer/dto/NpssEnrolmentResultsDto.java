package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NpssEnrolmentResultsDto {
    private String cif_id;
    private String enrollment_status;
    private String created_date;
    private LocalDateTime createdDate;
}