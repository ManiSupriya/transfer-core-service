package com.mashreq.transfercoreservice.promo.service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.promo.dto.PromoCodeTransactionRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeService {
    
    private final MobCommonService mobCommonService;
    private final BeneficiaryService beneficiaryService;
    
	public boolean validateAndSave(FundTransferRequestDTO request, String orderStatus, RequestMetaData metaData) {
		
		if(StringUtils.isBlank(request.getPromoCode())) {
        	return false;
        }
		
		try {
			validate(request, orderStatus, metaData);
			return true;
		} catch (Exception ex) {
			log.error("Error while validating promo code in fund transfer for code -> {}", htmlEscape(request.getPromoCode()), ex);
		}
		
		return false;
	}

	private void validate(FundTransferRequestDTO request, String orderStatus, RequestMetaData metaData) {
		String countryCode = beneficiaryService.getById(metaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), metaData).getBankCountryISO();

		PromoCodeTransactionRequestDto promoReq = PromoCodeTransactionRequestDto.builder()
				.countryOfResidence(countryCode)
				.serviceType(request.getServiceType())
				.txnAmount(request.getAmount())
				.txnCurrency(request.getTxnCurrency())
				.promoCode(request.getPromoCode())
				.orderStatus(orderStatus)
				.fromAccount(request.getFromAccount())
				.build();

		mobCommonService.validatePromoCode(promoReq);
	}
}
