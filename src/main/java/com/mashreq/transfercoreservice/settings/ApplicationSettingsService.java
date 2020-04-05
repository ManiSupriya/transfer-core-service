package com.mashreq.transfercoreservice.settings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author shahbazkh
 * @date 4/2/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "mob-ae-application-settings")
public class ApplicationSettingsService {

    public static final String DEFAULT_CHANNEL = "MOB";
    public static final String DEFAULT_REGION = "AE";
    private final ApplicationSettingsRepository applicationSettingsRepository;

    @Cacheable(sync = true)
    public String findBySettingKeyAndChannelNameAndRegion(String settingKey, String channelName, String region) {
        log.info("Find value for key = {}, channel = {}, region = {}", settingKey, channelName, region);
        return applicationSettingsRepository.findSettingValueByKey(settingKey, channelName, region);
    }
}
