package com.mashreq.transfercoreservice.fundtransfer.limits;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalUserLimitUsageService {

    private final DigitalUserLimitUsageMapper digitalUserLimitUsageMapper;
    private final DigitalUserLimitUsageRepository digitalUserLimitUsageRepository;

    public void insert(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO) {
        log.info("Store limit usage for CIF={} and beneficiaryTypeCode={} ",
                htmlEscape(digitalUserLimitUsageDTO.getCif()), htmlEscape(Long.toString(digitalUserLimitUsageDTO.getDigitalUserId())));
        DigitalUserLimitUsage digitalUserLimitUsage = digitalUserLimitUsageMapper.userLimitUsageDTOToEntity(digitalUserLimitUsageDTO);
        digitalUserLimitUsageRepository.save(digitalUserLimitUsage);
    }
}
