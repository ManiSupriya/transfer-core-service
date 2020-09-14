package com.mashreq.transfercoreservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mashreq.transfercoreservice.client.dto.CoreBankDetails;
import com.mashreq.transfercoreservice.config.feign.OmwFeignConfig;
import com.mashreq.transfercoreservice.config.feign.OmwHeaders;

@FeignClient(name = "omw-core", url = "${app.services.omw-core}")
public interface OmwCoreClient {

    @GetMapping(value = "/swift/bicinfo/{bicInfo}")
    CoreBankDetails searchAccounts(
    		@PathVariable("bicInfo") String bicInfo, 
    		@RequestHeader(OmwHeaders.ORG_ID) String orgId,
    		@RequestHeader(OmwHeaders.USER_ID) String userId,
    		@RequestHeader(OmwHeaders.SRC_MSG_ID) String srcMsgId
    		);
}

