package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.repository.TransferLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferLimitService {

	private final TransferLimitRepository repository;
	private final MaintenanceService maintenanceService;

	public TransferLimitResponseDto validateAndSaveTransferDetails(TransferLimitRequestDto limitRequestDto,
			String transactionRefNo) {
		try {
			if (repository.findByTransactionRefNo(transactionRefNo).isPresent()) {
				log.info("Duplicate entry found for the transaction reference no {}", transactionRefNo);
				return buildErrorMessage("TC-409", "Duplicate entry found for the transaction reference no");
			}
			BigDecimal convertedAmount = getAmountBasedOnCurrency(limitRequestDto);
			if (convertedAmount != null) {
				limitRequestDto.setAmount(convertedAmount);
			} else {
				log.error("Error occurred while converting Currency {} into AED", limitRequestDto.getAccountCurrency());
				return buildErrorMessage("TC-501", "Error occurred while converting currency into AED");
			}
			return saveTransferDetails(limitRequestDto, transactionRefNo);
		} catch (Exception e) {
			log.error("Error occurred while saving transfer details", e);
			return buildErrorMessage("TC-500", "Error occurred while saving transfer details");
		}
	}

	private TransferLimitResponseDto buildErrorMessage(String errorCode, String errorMessage) {
		return TransferLimitResponseDto.builder().success(false).errorCode(errorCode).errorMessage(errorMessage)
				.build();
	}

	public TransferLimitResponseDto saveTransferDetails(TransferLimitRequestDto limitDto, String transactionRefNo) {
		log.info("Storing transferred/converted amount {} for beneficiary {}", htmlEscape(limitDto.getAmount()),
				htmlEscape(limitDto.getBeneficiaryId()));
		repository.save(limitDto.toEntity(transactionRefNo));
		return TransferLimitResponseDto.builder().success(true).build();
	}

	private BigDecimal getAmountBasedOnCurrency(final TransferLimitRequestDto limitDto) {
		return "AED".equalsIgnoreCase(limitDto.getAccountCurrency()) ? limitDto.getAmount()
				: convertAmountInLocalCurrency(limitDto);
	}

	private BigDecimal convertAmountInLocalCurrency(final TransferLimitRequestDto limitDto) {
		CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
		currencyConversionRequestDto.setAccountNumber(limitDto.getAccountNumber());
		currencyConversionRequestDto.setAccountCurrency(limitDto.getAccountCurrency());
		currencyConversionRequestDto.setAccountCurrencyAmount(limitDto.getAmount());
		currencyConversionRequestDto.setTransactionCurrency("AED");

		CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
		if (currencyConversionDto == null) {
			return null;
		}
		return currencyConversionDto.getTransactionAmount();
	}
}