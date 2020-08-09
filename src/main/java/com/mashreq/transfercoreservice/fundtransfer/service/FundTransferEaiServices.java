package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfer.EAIServices;

public class FundTransferEaiServices extends EAIServices {

    public FundTransferEaiServices() {
        super();
    }

    /**
     * Constructor helper for SOAP webservice is returning Object type
     */
    public FundTransferEaiServices(Object o) {
        if(!(o instanceof EAIServices)) {
            throw new RuntimeException("Cannot construct FundTransferEaiServices object from type: "
                    + o.getClass().getSimpleName());
        }
        EAIServices eaiServices = (EAIServices) o;
        this.body = eaiServices.getBody();
        this.header = eaiServices.getHeader();
    }
}
