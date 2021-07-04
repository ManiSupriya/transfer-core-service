package com.mashreq.transfercoreservice.paylater.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.mashreq.transfercoreservice.common.HtmlEscapeCache;
import com.mashreq.transfercoreservice.paylater.enums.SIFrequencyType;
import com.mashreq.transfercoreservice.paylater.model.FundTransferOrder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderExecutionDateResolver {

	public static LocalDateTime getNextExecutionTime(FundTransferOrder order) {
		if(order.getOrderType().isRepeateable()) {
			LocalDate currentDate = LocalDate.now();
			if(!order.getStartDate().toLocalDate().equals(currentDate)) {
				log.debug("next execution date for orderId :{} is {}",HtmlEscapeCache.htmlEscape(order.getOrderId()),HtmlEscapeCache.htmlEscape(order.getStartDate()));
				return order.getStartDate();
			}
			SIFrequencyType frequencyType = order.getFrequency();
			LocalDateTime nextExecutionDateTime = LocalDateTime.of(currentDate.plusDays(frequencyType.getExecutionIntervalIndays()), LocalTime.of(0, 0));
			log.debug("next execution date for orderId :{} is {}", HtmlEscapeCache.htmlEscape(order.getOrderId()),HtmlEscapeCache.htmlEscape(nextExecutionDateTime));
			if(order.getEndDate().compareTo(nextExecutionDateTime) > 0) {
				return nextExecutionDateTime;
			}
		}
		return null;
	}
	
}
