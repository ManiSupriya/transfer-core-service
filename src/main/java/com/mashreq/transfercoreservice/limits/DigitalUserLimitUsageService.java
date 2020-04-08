package com.mashreq.transfercoreservice.limits;


import com.mashreq.transfercoreservice.fundtransfer.dto.DigitalUserLimitUsageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalUserLimitUsageService {

    private final DigitalUserLimitUsageMapper digitalUserLimitUsageMapper;
    private final DigitalUserLimitUsageRepository digitalUserLimitUsageRepository;

    public void insert(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO) {
        log.info("Store limit usage for CIF={} and beneficiaryTypeCode={} ",
                digitalUserLimitUsageDTO.getCif(), digitalUserLimitUsageDTO.getDigitalUserId());
        DigitalUserLimitUsage digitalUserLimitUsage = digitalUserLimitUsageMapper.userLimitUsageDTOToEntity(digitalUserLimitUsageDTO);
        digitalUserLimitUsageRepository.save(digitalUserLimitUsage);
    }
}
