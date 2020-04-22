package com.mashreq.transfercoreservice.client.service;


import com.mashreq.transfercoreservice.client.CustomerClient;
import com.mashreq.transfercoreservice.client.dto.CustomerDetailsDto;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerClient customerClient;

    public CustomerDetailsDto getCustomerDetails(final String cif) {
            log.info("[CustomerService] calling customer service client for getting customer details");
            return customerClient.getCustomerProfile(cif).getData();

    }
}
