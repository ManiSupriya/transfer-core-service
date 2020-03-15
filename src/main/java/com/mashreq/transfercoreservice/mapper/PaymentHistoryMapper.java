package com.mashreq.transfercoreservice.mapper;

import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.model.PaymentHistory;
import org.mapstruct.Mapper;

/**
 * @Author KetulkumarS
 */
@Mapper(componentModel = "spring")
public interface PaymentHistoryMapper {
    PaymentHistory paymentHistoryDtoToEntity(PaymentHistoryDTO paymentHistoryDTO);
}
