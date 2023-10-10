/*
package com.mashreq.transfercoreservice.paylater.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.impl.TwoFactorAuthRequiredCheckServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.OrderStatus;
import com.mashreq.transfercoreservice.paylater.model.FundTransferOrder;
import com.mashreq.transfercoreservice.paylater.model.Money;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.Ignore;

@Ignore
//@DataJpaTest
@ExtendWith(MockitoExtension.class)
public class FundTransferOrderRepositoryTest {
	
	@Autowired
    private FundTransferOrderRepository repository;
	@MockBean
	private ObjectMapper objectMapper;

	@BeforeEach
	public void init() {
		service = new TwoFactorAuthRequiredCheckServiceImpl(config, maintenanceService, beneficiaryService,
				transferLimitRepository);
		ReflectionTestUtils.setField(service, "localCurrency", localCurrency);
	}
    
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
		order.setOrderId("1234567");
		order = repository.saveAndFlush(order);
		assertNotNull(order.getId());
	}
	@Test()
	public void test_fail_without_ordertype() {
		FundTransferOrder order = new FundTransferOrder();
		order.setCif("0123456789");
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setStartDate(LocalDateTime.now().plusDays(1));
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setServiceType(ServiceType.INFT);
		order.setOrderId("1234567");
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		assertThrows(DataIntegrityViolationException.class,()->repository.saveAndFlush(order));
	}
	@Test()
	public void test_fail_without_serviceType() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setCif("0123456789");
		order.setCreatedBy("SYSTEM");
		order.setCreatedOn(LocalDateTime.now());
		order.setStartDate(LocalDateTime.now().plusDays(1));
		order.setSourceCurrency("AED");
		order.setOrderId("1234567");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		repository.saveAndFlush(order);
	}
	
	@Test()
	public void test_fail_without_cif() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setServiceType(ServiceType.INFT);
		order.setCreatedBy("SYSTEM");
		order.setOrderId("1234567");
		order.setCreatedOn(LocalDateTime.now());
		order.setStartDate(LocalDateTime.now().plusDays(1));
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		repository.saveAndFlush(order);
		assertThrows(DataIntegrityViolationException.class,()->repository.saveAndFlush(order));
	}
	
	@Test()
	public void test_fail_without_startDate() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		order.setServiceType(ServiceType.INFT);
		order.setCreatedBy("SYSTEM");
		order.setOrderId("1234567");
		order.setCreatedOn(LocalDateTime.now());
		order.setCif("0123456789");
		order.setSourceCurrency("AED");
		order.setOrderStatus(OrderStatus.PENDING);
		order.setTransactionValue(Money.valueOf(BigDecimal.TEN, "USD"));
		assertThrows(DataIntegrityViolationException.class,()->repository.saveAndFlush(order));
	}
	
	@Test()
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
		assertThrows(DataIntegrityViolationException.class,()->repository.saveAndFlush(order));
	}
		
	@Test()
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
		assertThrows(DataIntegrityViolationException.class,()->repository.saveAndFlush(order));
	}
}
*/
