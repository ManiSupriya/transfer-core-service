package com.mashreq.transfercoreservice.fundtransfer.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by KrishnaKo on 04/06/2023
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractProjectDetails {

    private String projectName;
    private String unitPayment;
    private String unitId;
    private String module;
    private String depositTfrNo;
}
