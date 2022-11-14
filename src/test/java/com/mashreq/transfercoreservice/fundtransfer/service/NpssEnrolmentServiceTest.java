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
    public void testEnrolment() {
        NpssEnrolmentRepo result = new NpssEnrolmentRepo();
        RequestMetaData metaData = getMetaData("012960010");
        when(npssEnrolmentRepository.getEnrolmentStatus(any())).thenReturn(result);
        NpssEnrolmentStatusResponseDTO response = npssEnrolmentService.checkEnrolment(metaData);
        Assertions.assertEquals(true, response.getAskForEnrolment());
    }

    private RequestMetaData getMetaData(String cif) {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif(cif);
        return metaData;
    }
}
