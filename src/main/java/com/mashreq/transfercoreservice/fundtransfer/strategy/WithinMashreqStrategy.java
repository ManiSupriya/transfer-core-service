//package com.mashreq.transfercoreservice.fundtransfer.strategy;
//
//import com.mashreq.ms.exceptions.GenericExceptionHandler;
//import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
//import com.mashreq.transfercoreservice.enums.MwResponseStatus;
//import com.mashreq.transfercoreservice.fundtransfer.dto.*;
//import com.mashreq.transfercoreservice.limits.DigitalUserLimitUsageService;
//import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferService;
//import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;
//import com.mashreq.transfercoreservice.limits.LimitValidator;
//import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
//import com.mashreq.transfercoreservice.model.DigitalUser;
//import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;
//import static com.mashreq.transfercoreservice.errors.TransferErrorCode.PAYMENT_FAILURE;
//
///**
// * @author shahbazkh
// * @date 3/15/20
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class WithinMashreqStrategy implements FundTransferStrategy {
//
//    private final DigitalUserRepository digitalUserRepository;
//    private final FundTransferService fundTransferService;
//    private final LimitValidator limitValidator;
//    private final PaymentHistoryService paymentHistoryService;
//    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
//
//    @Override
//    public PaymentHistoryDTO transferFund(FundTransferMetadata fundTransferMetadata, FundTransferRequestDTO request) {
//
//        log.info("Finding Digital User for CIF-ID {}", fundTransferMetadata.getPrimaryCif());
//        DigitalUser digitalUser = getDigitalUser(fundTransferMetadata);
//
//        log.info("Creating  User DTO");
//        UserDTO userDTO = createUserDTO(fundTransferMetadata, digitalUser);
//
//        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), request.getAmount());
//        log.info("Limit Validation successful ");
//
//        CoreFundTransferResponseDto response = fundTransferService.transferFund(request);
//
//        // If payment is success then insert into the limit usage
//        if (response.getMwResponseStatus().equals(MwResponseStatus.S)) {
//            String versionUuid = validationResult.getLimitVersionUuid();
//            DigitalUserLimitUsageDTO digitalUserLimitUsageDTO = generateUserLimitUsage(request, userDTO, fundTransferMetadata, versionUuid);
//            log.info("Inserting into limits table {} ", digitalUserLimitUsageDTO);
//            digitalUserLimitUsageService.insert(digitalUserLimitUsageDTO);
//        }
//
//        // Insert payment history irrespective of mw payment fails or success
//        PaymentHistoryDTO paymentHistoryDTO = generatePaymentHistory(request, response, userDTO, fundTransferMetadata);
//
//        log.info("Inserting into Payments History table {} ", fundTransferMetadata.getChannelTraceId(), paymentHistoryDTO);
//        paymentHistoryService.insert(paymentHistoryDTO);
//
//        if (MwResponseStatus.F.equals(response.getMwResponseStatus())) {
//            GenericExceptionHandler.handleError(PAYMENT_FAILURE, String.format(PAYMENT_FAILURE.getErrorMessage(),
//                    response.getMwReferenceNo()));
//        }
//
//
//        return paymentHistoryDTO;
//    }
//
//    private DigitalUser getDigitalUser(FundTransferMetadata fundTransferMetadata) {
//        Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(fundTransferMetadata.getPrimaryCif());
//        if (!digitalUserOptional.isPresent()) {
//            GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
//        }
//        log.info("Digital User found successfully {} ", digitalUserOptional.get());
//        return digitalUserOptional.get();
//    }
//
//    private UserDTO createUserDTO(FundTransferMetadata fundTransferMetadata, DigitalUser digitalUser) {
//        UserDTO userDTO = new UserDTO();
//        userDTO.setCifId(fundTransferMetadata.getPrimaryCif());
//        userDTO.setUserId(digitalUser.getId());
//        userDTO.setSegmentId(digitalUser.getDigitalUserGroup().getSegment().getId());
//        userDTO.setCountryId(digitalUser.getDigitalUserGroup().getCountry().getId());
//
//        log.info("User DTO  created {} ", userDTO);
//        return userDTO;
//    }
//
//    private DigitalUserLimitUsageDTO generateUserLimitUsage(FundTransferRequestDTO request, UserDTO userDTO,
//                                                            FundTransferMetadata fundTransferMetadata, String versionUuid) {
//        return DigitalUserLimitUsageDTO.builder()
//                .digitalUserId(userDTO.getUserId())
//                .cif(fundTransferMetadata.getPrimaryCif())
//                .channel(fundTransferMetadata.getChannel())
//                //.beneficiaryTypeCode(request.getBillerType())
//                .paidAmount(request.getAmount())
//                .versionUuid(versionUuid)
//                .createdBy(String.valueOf(userDTO.getUserId()))
//                .build();
//
//    }
//
//    PaymentHistoryDTO generatePaymentHistory(FundTransferRequestDTO request, CoreFundTransferResponseDto coreResponse, UserDTO userDTO,
//                                             FundTransferMetadata fundTransferMetadata) {
//
//        //convert dto
//        return PaymentHistoryDTO.builder()
//                .cif(fundTransferMetadata.getPrimaryCif())
//                .userId(userDTO.getUserId())
//                .accountTo(request.getToAccount())
//                .beneficiaryTypeCode(request.getServiceType())
//                .channel(fundTransferMetadata.getChannel())
//                //.billRefNo(coreResponse.getBillRefNo())
//                .ipAddress(fundTransferMetadata.getChannelHost())
//                .paidAmount(request.getAmount())
//                //.dueAmount(request.getDueAmount())
//                //.toCurrency(PaymentConstants.BILL_PAYMENT_TO_CURRENCY)
//                //.status(mwResponse.getMwResponseStatus().name())
//                .mwReferenceNo(coreResponse.getMwReferenceNo())
//                .mwResponseCode(coreResponse.getMwResponseCode())
//                .mwResponseDescription(coreResponse.getMwResponseDescription())
//                .accountFrom(request.getFromAccount())
//                //.encryptedCardFrom(request.getDebitAccountNo())
////                .encryptedCardFromFourdigit(
////                        StringUtils.isEmpty(request.getDebitAccountNo()) ? null :
////                                (request.getDebitAccountNo().substring(request.getDebitAccountNo().length() - 4)))
//                .build();
//
//    }
//}
