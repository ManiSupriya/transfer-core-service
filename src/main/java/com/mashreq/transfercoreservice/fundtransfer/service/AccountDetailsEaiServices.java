package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.accountservices.mbcdm.accountdetails.EAIServices;

public class AccountDetailsEaiServices extends EAIServices {

    public AccountDetailsEaiServices() {
        super();
    }

    /**
     * Constructor helper for SOAP webservice is returning Object type
     */
    public AccountDetailsEaiServices(Object o) {
        super();
        if(!(o instanceof EAIServices)) {
            throw new RuntimeException("Passing wrong object type to AccountDetailsEaiServices constructor. "
                    + "Must be mbcdm.accountsummary.EAIServices");
        }
        EAIServices eaiServices = (EAIServices) o;
        this.body = eaiServices.getBody();
        this.header = eaiServices.getHeader();
    }
}
