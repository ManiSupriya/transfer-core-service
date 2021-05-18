package com.mashreq.transfercoreservice.promo.service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.promo.dto.PromoCodeRequestDto;
import com.mashreq.transfercoreservice.promo.model.PromoCodeTransaction;
import com.mashreq.transfercoreservice.promo.repository.PromoCodeTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeService {
    
    private final PromoCodeTransactionRepository promoRepo;
    private final MobCommonService mobCommonService;
    private final BeneficiaryService beneficiaryService;
    
	public boolean validateAndSave(FundTransferRequestDTO request, String orderStatus, RequestMetaData metaData) {
		
		if(StringUtils.isBlank(request.getPromoCode())) {
        	return false;
        }

		PromoCodeRequestDto promoReq = null;
		try {
			
			promoReq = validate(request, metaData);
			
			savePromoTransaction(request, orderStatus, metaData);
			
			return true;
			
		} catch (Exception ex) {
			log.error("Error while validating promo code in fund transfer for code -> {}", htmlEscape(request.getPromoCode()), htmlEscape(promoReq), ex);
		}
		return false;
	}

	private PromoCodeRequestDto validate(FundTransferRequestDTO request, RequestMetaData metaData) {
		String countryCode = beneficiaryService.getById(metaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), metaData).getBankCountryISO();

		PromoCodeRequestDto promoReq = PromoCodeRequestDto.builder()
				.countryOfResidence(countryCode)
				.serviceType(request.getServiceType())
				.txnAmount(request.getAmount())
				.txnCurrency(request.getTxnCurrency())
				.promoCode(request.getPromoCode())
				.build();

		mobCommonService.validatePromoCode(promoReq);
		
		return promoReq;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	private void savePromoTransaction(FundTransferRequestDTO request, String orderStatus, RequestMetaData metaData) {
		PromoCodeTransaction promoCodeTransaction = PromoCodeTransaction.builder()
				.cif(metaData.getPrimaryCif())
				.code(request.getPromoCode())
				.createdOn(new Date())
				.sndrAcNum(request.getFromAccount())
				.transAmnt(request.getAmount())
				.orderStatus(orderStatus)
				.build();
		
		promoRepo.save(promoCodeTransaction);

	}
}
