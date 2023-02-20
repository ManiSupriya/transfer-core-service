package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.model.ServiceType;
import com.mashreq.transfercoreservice.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.MIN_AMOUNT_LIMIT_REACHED;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.MIN_LIMIT_VALIDATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinTransactionAmountValidator implements Validator<FundTransferRequestDTO> {

    public static final String TRANSFER_AMOUNT_FOR_MIN_VALIDATION = "transfer-amount-for-min-validation";
    private final ServiceTypeRepository serviceTypeRepository;
    private final AsyncUserEventPublisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        Optional<ServiceType> serviceType = getServiceType(request.getServiceType());
        if (serviceType.isPresent() && Objects.nonNull(serviceType.get().getMinAmount())) {

            BigDecimal minAmount = new BigDecimal((serviceType.get().getMinAmount()));
            BigDecimal transferAmountInSourceCurrency = context.get(TRANSFER_AMOUNT_FOR_MIN_VALIDATION, BigDecimal.class);

            String remarks = getRemarks(metadata.getPrimaryCif(), transferAmountInSourceCurrency, request.getServiceType(), minAmount);
            if (transferAmountInSourceCurrency.compareTo(minAmount) < 0) {
                auditEventPublisher.publishFailureEvent(MIN_LIMIT_VALIDATION, metadata, remarks,
                        MIN_AMOUNT_LIMIT_REACHED.getCustomErrorCode(), MIN_AMOUNT_LIMIT_REACHED.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(MIN_AMOUNT_LIMIT_REACHED).build();
            }
            auditEventPublisher.publishSuccessEvent(MIN_LIMIT_VALIDATION, metadata, remarks);
            log.info("Min transaction amount validation successful");

        }
        return ValidationResult.builder().success(true).build();
    }

    private Optional<ServiceType> getServiceType(String serviceTypeCode) {
        return serviceTypeRepository.findByCodeEquals(serviceTypeCode);
    }

    private String getRemarks(String cif, BigDecimal transferAmount, String serviceType, BigDecimal minAmount) {
        return String.format(
                "Cif=%s,TransferAmountInSourceCurrency=%s,ServiceType=%s,configuredMinAmount=%s",
                cif,
                transferAmount,
                serviceType,
                minAmount
        );
    }
}
