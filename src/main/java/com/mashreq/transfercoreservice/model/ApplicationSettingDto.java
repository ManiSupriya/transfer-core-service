package com.mashreq.transfercoreservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSettingDto {
    private String settingKey;
    private String settingValue;
    private String group;

}
