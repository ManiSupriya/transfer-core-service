package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;


import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.QRT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferEligibilityProxy {

	private Map<ServiceType, List<TransferEligibilityService>> transferEligibilityServiceMap;
	private final INFTAccountEligibilityService inftAccountEligibilityService;
	private final LocalAccountEligibilityService localAccountEligibilityService;
	private final OwnAccountEligibilityService ownAccountEligibilityService;
	private final WithinAccountEligibilityService withinAccountEligibilityService;
	private final QRAccountEligibilityService qrAccountEligibilityService;
	private final DigitalUserRepository digitalUserRepository;

	@PostConstruct
	public void init() {
		transferEligibilityServiceMap = new EnumMap<>(ServiceType.class);
		transferEligibilityServiceMap.put(INFT, Arrays.asList(inftAccountEligibilityService, qrAccountEligibilityService));
		transferEligibilityServiceMap.put(QRT, 	Arrays.asList(inftAccountEligibilityService, qrAccountEligibilityService));
		//transferEligibilityServiceMap.put(QRIN, Arrays.asList(inftAccountEligibilityService, qrAccountEligibilityService));
		//transferEligibilityServiceMap.put(QRPK, Arrays.asList(inftAccountEligibilityService, qrAccountEligibilityService));
		transferEligibilityServiceMap.put(LOCAL, Arrays.asList(localAccountEligibilityService, inftAccountEligibilityService));
		transferEligibilityServiceMap.put(WAMA, Arrays.asList(withinAccountEligibilityService));
		transferEligibilityServiceMap.put(WYMA, Arrays.asList(ownAccountEligibilityService));
	}

	public Map<ServiceType,EligibilityResponse> getEligibleServiceTypes(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request) {
		
		log.info("Starting fund transfer eligibility check for {} ", htmlEscape(request.getServiceType()));
		
		Map<ServiceType,EligibilityResponse> serviceTypes = new HashMap<>();
		
		ServiceType serviceType = ServiceType.getServiceByType(request.getServiceType());

		log.info("Finding Digital User for CIF-ID {}", htmlEscape(metaData.getPrimaryCif()));
		DigitalUser digitalUser = getDigitalUser(metaData);

		log.info("Creating  User DTO");
		UserDTO userDTO = createUserDTO(metaData, digitalUser);

		for(TransferEligibilityService eligibilityService : transferEligibilityServiceMap.get(serviceType)) {
			try {
				eligibilityService.modifyServiceType(request);
				serviceTypes.put(eligibilityService.getServiceType(),eligibilityService.checkEligibility(metaData, request, userDTO));
			} catch(GenericException ge) {
				log.error("Validation error while checking for eligibility for service type {}", serviceType, ge);
				serviceTypes.put(eligibilityService.getServiceType(),
						EligibilityResponse.builder()
						.status(FundsTransferEligibility.NOT_ELIGIBLE)
						.errorCode(ge.getErrorCode())
						.errorMessage(ge.getMessage())
						.errorDetails(ge.getErrorDetails())
						.build());
			}
		}
		return serviceTypes;
	}

	private DigitalUser getDigitalUser(RequestMetaData fundTransferMetadata) {
		Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(fundTransferMetadata.getPrimaryCif());
		if (!digitalUserOptional.isPresent()) {
			GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
		}
		log.info("Digital User found successfully {} ", digitalUserOptional.get());

		return digitalUserOptional.get();
	}

	private UserDTO createUserDTO(RequestMetaData fundTransferMetadata, DigitalUser digitalUser) {
		UserDTO userDTO = new UserDTO();
		userDTO.setCifId(fundTransferMetadata.getPrimaryCif());
		userDTO.setUserId(digitalUser.getId());
		userDTO.setSegmentId(digitalUser.getDigitalUserGroup().getSegment().getId());
		userDTO.setCountryId(digitalUser.getDigitalUserGroup().getCountry().getId());
		userDTO.setLocalCurrency(digitalUser.getDigitalUserGroup().getCountry().getLocalCurrency());
		userDTO.setDeviceRegisteredForPush(digitalUser.getDeviceidRegisteredForPushnotify());
		userDTO.setDeviceInfo(digitalUser.getDeviceInfo());

		log.info("User DTO  created {} ", userDTO);
		return userDTO;
	}

}
