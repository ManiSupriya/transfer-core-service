package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.QRDealDetails;
import com.mashreq.transfercoreservice.fundtransfer.mapper.QRDealsMapper;
import com.mashreq.transfercoreservice.model.QRDealsEntity;
import com.mashreq.transfercoreservice.repository.QRDealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QRDealsServiceImpl implements  QRDealsService{

    @Autowired
    private QRDealsMapper qrDealsMapper;

    @Autowired
    private QRDealRepository qrDealRepository;

    @Override
    public QRDealDetails getQRDealDetails(String cif, String country) {
        QRDealDetails qrDealDetails = null;
        QRDealsEntity qrDealsEntity = qrDealRepository.findDealsByCIFAndCountry(cif, country);
        if(qrDealsEntity != null) {
            qrDealDetails = qrDealsMapper.entityToDto(qrDealsEntity);
        }
        return qrDealDetails;
    }

    // TODO need to confirm whether throw an exception or not.
    @Override
    public void updateQRDeals(String cif, Float utilizedAmount) {
        QRDealsEntity qrDealsEntity = qrDealRepository.findDealsByCif(cif);
        if(qrDealsEntity != null) {
            qrDealsEntity.setUtilizedLimitAmount(utilizedAmount);
            qrDealRepository.save(qrDealsEntity);
        }
    }
}
