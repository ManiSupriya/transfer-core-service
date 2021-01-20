package com.mashreq.transfercoreservice.client.mobcommon.dto;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MoneyTransferPurposeDto {

    private String purposeCode;
    private String purposeDesc;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoneyTransferPurposeDto that = (MoneyTransferPurposeDto) o;
        return purposeCode.equals(that.purposeCode) &&
                purposeDesc.equals(that.purposeDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purposeCode, purposeDesc);
    }

}
