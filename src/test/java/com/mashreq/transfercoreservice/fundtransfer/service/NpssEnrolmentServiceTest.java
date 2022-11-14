package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepo;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentStatusResponseDTO;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NpssEnrolmentServiceTest {

    @Mock
    private NpssEnrolmentRepository npssEnrolmentRepository;
    @InjectMocks
    private NpssEnrolmentService npssEnrolmentService;

    @Test
    public void testEnrolmentFail() {
        RequestMetaData metaData = getMetaData("012960010");
        when(npssEnrolmentRepository.getEnrolmentStatus(any())).thenReturn(null);
        npssEnrolmentService.checkEnrolment(metaData);
    }

//    @Test
//    public void testEnrolmentWithSuccess() {
//        NpssEnrolmentRepo result = new NpssEnrolmentRepo();
//        result.setCif_id("012960010");
//        RequestMetaData metaData = getMetaData("012960010");
//        when(npssEnrolmentRepository.getEnrolmentStatus(any())).thenReturn(result);
//        NpssEnrolmentStatusResponseDTO response = npssEnrolmentService.checkEnrolment(metaData);
//    }

    private RequestMetaData getMetaData(String cif) {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif(cif);
        return metaData;
    }
}
