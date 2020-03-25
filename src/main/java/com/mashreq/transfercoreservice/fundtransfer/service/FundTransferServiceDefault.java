package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.account.mbcdm.IBANDetailsReqType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.ibandetails.EAIServices;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.fundtransfer.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.strategy.*;
import com.mashreq.transfercoreservice.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;
import static com.mashreq.transfercoreservice.fundtransfer.ServiceType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferServiceDefault implements FundTransferService {

    private final CoreTransferService coreTransferService;
    private final DigitalUserRepository digitalUserRepository;
    private final LimitValidator limitValidator;
    private final PaymentHistoryService paymentHistoryService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final OwnAccountStrategy ownAccountStrategy;
    private final WithinMashreqStrategy withinMashreqStrategy;
    private final LocalFundTransferStrategy localFundTransferStrategy;
    private final CharityStrategy charityStrategy;
    private final FundTransferMWService fundTransferMWService;

    private EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;

    @PostConstruct
    public void init() {
        fundTransferStrategies = new EnumMap<>(ServiceType.class);
        fundTransferStrategies.put(OWN_ACCOUNT, ownAccountStrategy);
        fundTransferStrategies.put(WITHIN_MASHREQ, withinMashreqStrategy);
        fundTransferStrategies.put(CHARITY_ACCOUNT, charityStrategy);
        fundTransferStrategies.put(LOCAL,localFundTransferStrategy);
    }

    @Override
    public PaymentHistoryDTO transferFund(FundTransferMetadata metadata, FundTransferRequestDTO request) {
        log.info("Starting fund transfer for {} ", request.getServiceType());
        CoreFundTransferResponseDto response = null;

        FundTransferStrategy strategy = fundTransferStrategies.get(ServiceType.getServiceByType(request.getServiceType()));
        strategy.execute(request, metadata);

        log.info("Finding Digital User for CIF-ID {}", metadata.getPrimaryCif());
        DigitalUser digitalUser = getDigitalUser(metadata);

        log.info("Creating  User DTO");
        UserDTO userDTO = createUserDTO(metadata, digitalUser);

        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), request.getAmount());
        log.info("Limit Validation successful");

        if (!ServiceType.LOCAL.getName().equals(request.getServiceType())) {
            response = coreTransferService.transferFundsBetweenAccounts(request);
        }
        else {
            response = fundTransferMWService.sendMoneyToIBAN(metadata, request);
        }


        if (response.getMwResponseStatus().equals(MwResponseStatus.S)) {
            String versionUuid = validationResult.getLimitVersionUuid();
            DigitalUserLimitUsageDTO digitalUserLimitUsageDTO = generateUserLimitUsage(request, userDTO, metadata, versionUuid);
            log.info("Inserting into limits table {} ", digitalUserLimitUsageDTO);
            digitalUserLimitUsageService.insert(digitalUserLimitUsageDTO);
        }

        // Insert payment history irrespective of mw payment fails or success
        PaymentHistoryDTO paymentHistoryDTO = generatePaymentHistory(request, response, userDTO, metadata);

        log.info("Inserting into Payments History table {} ", paymentHistoryDTO);
        paymentHistoryService.insert(paymentHistoryDTO);

        if (MwResponseStatus.F.equals(response.getMwResponseStatus())) {
            GenericExceptionHandler.handleError(response.getTransferErrorCode(),
                    String.format("%s : %s ", request.getFinTxnNo(), response.getExternalErrorMessage()));
        }

        return paymentHistoryDTO;
    }


    private DigitalUser getDigitalUser(FundTransferMetadata fundTransferMetadata) {
        Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(fundTransferMetadata.getPrimaryCif());
        if (!digitalUserOptional.isPresent()) {
            GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
        }
        log.info("Digital User found successfully {} ", digitalUserOptional.get());
        return digitalUserOptional.get();
    }

    private UserDTO createUserDTO(FundTransferMetadata fundTransferMetadata, DigitalUser digitalUser) {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId(fundTransferMetadata.getPrimaryCif());
        userDTO.setUserId(digitalUser.getId());
        userDTO.setSegmentId(digitalUser.getDigitalUserGroup().getSegment().getId());
        userDTO.setCountryId(digitalUser.getDigitalUserGroup().getCountry().getId());

        log.info("User DTO  created {} ", userDTO);
        return userDTO;
    }

    private DigitalUserLimitUsageDTO generateUserLimitUsage(FundTransferRequestDTO request, UserDTO userDTO,
                                                            FundTransferMetadata fundTransferMetadata, String versionUuid) {
        return DigitalUserLimitUsageDTO.builder()
                .digitalUserId(userDTO.getUserId())
                .cif(fundTransferMetadata.getPrimaryCif())
                .channel(fundTransferMetadata.getChannel())
                .beneficiaryTypeCode(request.getServiceType())
                .paidAmount(request.getAmount())
                .versionUuid(versionUuid)
                .createdBy(String.valueOf(userDTO.getUserId()))
                .build();

    }

    PaymentHistoryDTO generatePaymentHistory(FundTransferRequestDTO request, CoreFundTransferResponseDto coreResponse, UserDTO userDTO,
                                             FundTransferMetadata fundTransferMetadata) {

        //convert dto
        return PaymentHistoryDTO.builder()
                .cif(fundTransferMetadata.getPrimaryCif())
                .userId(userDTO.getUserId())
                .accountTo(request.getToAccount())
                .beneficiaryTypeCode(request.getServiceType())
                .channel(fundTransferMetadata.getChannel())
                //.billRefNo(coreResponse.getBillRefNo())
                .ipAddress(fundTransferMetadata.getChannelHost())
                .paidAmount(request.getAmount())
                //.dueAmount(request.getDueAmount())
                //.toCurrency(PaymentConstants.BILL_PAYMENT_TO_CURRENCY)
                .status(coreResponse.getMwResponseStatus().name())
                .mwReferenceNo(coreResponse.getMwReferenceNo())
                .mwResponseCode(coreResponse.getMwResponseCode())
                .mwResponseDescription(coreResponse.getMwResponseDescription())
                .accountFrom(request.getFromAccount())
                .financialTransactionNo(request.getFinTxnNo())
                //.encryptedCardFrom(request.getDebitAccountNo())
//                .encryptedCardFromFourdigit(
//                        StringUtils.isEmpty(request.getDebitAccountNo()) ? null :
//                                (request.getDebitAccountNo().substring(request.getDebitAccountNo().length() - 4)))
                .build();

    }
}
