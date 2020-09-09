package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
	Optional<Country> findByLocalCurrencyEqualsIgnoreCase(String localCurrency);
}
