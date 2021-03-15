package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.http.RequestMetaData;

/**
 * @author shahbazkh
 */
public interface Validator<T> {

    default ValidationResult validate(final T request, final RequestMetaData metadata) {
        return validate(request, metadata, null);
    }

    ValidationResult validate(T request, RequestMetaData metadata, ValidationContext context);
}
