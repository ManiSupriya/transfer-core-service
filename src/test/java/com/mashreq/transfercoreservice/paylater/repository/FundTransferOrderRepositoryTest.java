package com.mashreq.transfercoreservice.paylater.repository;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.OrderStatus;
import com.mashreq.transfercoreservice.paylater.model.FundTransferOrder;
import com.mashreq.transfercoreservice.paylater.model.Money;

@DataJpaTest
@RunWith(SpringRunner.class)
public class FundTransferOrderRepositoryTest {
	
	@Autowired
    private FundTransferOrderRepository repository;
	@MockBean
	private ObjectMapper objectMapper;
    
	@Test
	public void test_successful_save_with_all_required_fields() {
		FundTransferOrder order = new FundTransferOrder();
		order.setCif("0123456789");
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setStartDate(LocalDateTime.now().plusDays(1));
		order.setSourceCurrency("AED");
		order.setOrderType(FTOrderType.PL);
		order.setOrderStatus(OrderStatus.PENDING);
		order.setServiceType(ServiceType.INFT);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		order = repository.saveAndFlush(order);
		assertNotNull(order.getId());
	}
	@Test(expected = DataIntegrityViolationException.class)
	public void test_fail_without_ordertype() {
		FundTransferOrder order = new FundTransferOrder();
		order.setCif("0123456789");
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setStartDate(LocalDateTime.now().plusDays(1));
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setServiceType(ServiceType.INFT);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		repository.saveAndFlush(order);
	}
	@Test(expected = DataIntegrityViolationException.class)
	public void test_fail_without_serviceType() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setCif("0123456789");
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setStartDate(LocalDateTime.now().plusDays(1));
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		repository.saveAndFlush(order);
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void test_fail_without_cif() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setServiceType(ServiceType.INFT);
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setStartDate(LocalDateTime.now().plusDays(1));
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		repository.saveAndFlush(order);
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void test_fail_without_startDate() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setServiceType(ServiceType.INFT);
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setCif("0123456789");
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		repository.saveAndFlush(order);
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void test_fail_without_money() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setServiceType(ServiceType.INFT);
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setCif("0123456789");
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setCreatedOn(LocalDateTime.now());
		repository.saveAndFlush(order);
	}
		
	@Test(expected = DataIntegrityViolationException.class)
	public void test_fail_without_sourceCurrency() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setServiceType(ServiceType.INFT);
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setCif("0123456789");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setCreatedOn(LocalDateTime.now());
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		repository.saveAndFlush(order);
	}
}
