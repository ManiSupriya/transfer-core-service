package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;

import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;

import static com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitType.getServiceByCountry;
import static java.lang.Long.valueOf;


@RequiredArgsConstructor
@Slf4j
@Service
public class QuickRemitStrategy implements QuickRemitFundTransfer {

    private final QuickRemitIndiaStrategy quickRemitIndiaStrategy;
    private final QuickRemitPakistanStrategy quickRemitPakistanStrategy;
    private final QuickRemitInstaRemStrategy quickRemitInstaRemStrategy;
    private final BeneficiaryService beneficiaryService;
    private EnumMap<QuickRemitType, QuickRemitFundTransfer> fundTransferStrategies;

    @Value("${app.uae.currency.iso:784}")
    private String srcCurrencyIso;

    @Value("${app.uae.country.iso:AE}")
    private String srcCountryIso;

    @PostConstruct
    public void init() {
        fundTransferStrategies = new EnumMap<>(QuickRemitType.class);
        fundTransferStrategies.put(QuickRemitType.INDIA, quickRemitIndiaStrategy);
        fundTransferStrategies.put(QuickRemitType.PAKISTAN, quickRemitPakistanStrategy);
        fundTransferStrategies.put(QuickRemitType.INSTAREM,quickRemitInstaRemStrategy);
    }


    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO, ValidationContext context) {
        log.info("Quick remit flow initiated");
        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById((metadata.getPrimaryCif()), valueOf(request.getBeneficiaryId()));
        final String countryCodeISo = beneficiaryDto.getBeneficiaryCountryISO();
        log.info("Initiating Quick Remit transfer to {}", countryCodeISo);
        final ValidationContext validateContext = new ValidationContext();
        validateContext.add("beneficiary-dto", beneficiaryDto);
        validateContext.add("src-currency-iso", srcCurrencyIso);
        validateContext.add("src-country-iso", srcCountryIso);
        final QuickRemitFundTransfer quickRemitFundTransfer = fundTransferStrategies.get(getServiceByCountry(countryCodeISo));
        return quickRemitFundTransfer.execute(request, metadata, userDTO, validateContext);
    }

}
