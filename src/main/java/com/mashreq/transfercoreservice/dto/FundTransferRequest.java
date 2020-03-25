package com.mashreq.transfercoreservice.dto;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class FundTransferRequest {

    private FundTransferMetadata fundTransferMetadata;
    private FundTransferRequestDTO fundTransferRequestDTO;
    private AccountDetailsDTO accountDetailsDTO;
    private BeneficiaryDto beneficiaryDto;
}
