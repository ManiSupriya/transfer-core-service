package com.mashreq.transfercoreservice.fundtransfer.mapper;


import com.mashreq.transfercoreservice.fundtransfer.dto.QRDealDetails;
import com.mashreq.transfercoreservice.model.QRDealsEntity;
import org.mapstruct.Mapper;


/**
 * Used to map the entity to DTO and vice-versa
 * @author ThanigachalamP
 */
@Mapper(componentModel = "spring")
public interface QRDealsMapper {

    QRDealDetails entityToDto(QRDealsEntity qrDealsEntity);

}
