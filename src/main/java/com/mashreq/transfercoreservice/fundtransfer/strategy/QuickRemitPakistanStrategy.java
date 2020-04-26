package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.CustomerService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils.generateBeneficiaryAddress;


@RequiredArgsConstructor
@Slf4j
@Service
public class QuickRemitPakistanStrategy implements QuickRemitFundTransfer {


    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO, ValidationContext validationContext) {
        log.info("Quick remit to PAKISTAN starts");
        return FundTransferResponse.builder().responseDto(CoreFundTransferResponseDto.builder()
                .externalErrorMessage(null)
                .mwReferenceNo("mock1234")
                .mwResponseDescription("Success")
                .mwResponseStatus(MwResponseStatus.S)
                .mwResponseCode("EAI-FCI-BRK-000")
                .transactionRefNo(request.getFinTxnNo())
                .build())
                .limitVersionUuid("limitVersionUuid")
                .limitUsageAmount(BigDecimal.TEN)
                .build();

    }

}
