package com.mashreq.transfercoreservice.paylater.utils;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.mashreq.transfercoreservice.paylater.repository.FundTransferOrderRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SequenceNumberGenerator {
	private final FundTransferOrderRepository repository;
	
	public String getNextOrderId() {
		Long nextFTOrderSequence = repository.getNextFTOrderSequence();
		String yr = String.format("%02d",LocalDate.now().getYear()%100);
		String mn = String.format("%02d",LocalDate.now().getMonthValue());
		return new StringBuilder().append(yr).append(mn).append(nextFTOrderSequence).toString();
	}
}
