package com.mashreq.transfercoreservice.settings;

import com.mashreq.transfercoreservice.model.ApplicationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author shahbazkh
 * @date 4/2/20
 */
@Repository
public interface ApplicationSettingsRepository extends JpaRepository<ApplicationSetting, Long> {

    @Query("SELECT settingValue FROM ApplicationSetting ap WHERE ap.settingKey = ?1 and ap.channelName = ?2 and ap.region = ?3 and ap.deleted = false")
    String findSettingValueByKey(String settingKey, String channel, String region);
}
