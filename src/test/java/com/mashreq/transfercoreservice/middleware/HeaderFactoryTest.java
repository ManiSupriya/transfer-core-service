package com.mashreq.transfercoreservice.middleware;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class HeaderFactoryTest {
	@InjectMocks
	private HeaderFactory factory;
	@Mock
	private SoapServiceProperties soapServiceProperties;
	
	
	@Test
	public void test_getHeader_forDBFC() {
		Mockito.when(soapServiceProperties.getTftUserId()).thenReturn("MOMMUSER");
		HeaderType header = factory.getHeader("service", "DBFC", "msgId-1233");
		assertNotNull(header);
		assertEquals("MOMMUSER", header.getUserId());
	}

	@Test
	public void test_getHeader_forDBLC() {
		Mockito.when(soapServiceProperties.getTftUserId()).thenReturn("MOMMUSER");
		HeaderType header = factory.getHeader("service", "DBLC", "msgId-1233");
		assertNotNull(header);
		assertEquals("MOMMUSER", header.getUserId());
		
	}
	
	@Test
	public void test_getHeader_forOTHERS() {
		Mockito.when(soapServiceProperties.getTftUserId()).thenReturn("MOMMUSER");
		HeaderType header = factory.getHeader("service", "AACT", "msgId-1233");
		assertNotNull(header);
		assertEquals("MOMMUSER", header.getUserId());
	}
	
	@Test
	public void test_getHeader_forNULL() {
		Mockito.when(soapServiceProperties.getTftUserId()).thenReturn("MOMMUSER");
		HeaderType header = factory.getHeader("service", null, "msgId-1233");
		assertNotNull(header);
		assertEquals("MOMMUSER", header.getUserId());
	}
	
	@Test
	public void test_getHeader_forEmpty() {
		Mockito.when(soapServiceProperties.getTftUserId()).thenReturn("MOMMUSER");
		HeaderType header = factory.getHeader("service", "AACT", "msgId-1233");
		assertNotNull(header);
		assertEquals("MOMMUSER", header.getUserId());
		
	}
}
