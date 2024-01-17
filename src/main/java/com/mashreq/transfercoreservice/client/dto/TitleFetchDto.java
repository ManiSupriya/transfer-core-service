package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by KrishnaKo on 05/01/2024
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TitleFetchDto {
    private String title;
    private String accountType;
    private String idType;
    private String id;
    private String eid;
}
