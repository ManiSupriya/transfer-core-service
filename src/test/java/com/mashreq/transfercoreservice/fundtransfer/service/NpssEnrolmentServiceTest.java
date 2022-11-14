package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepo;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class NpssEnrolmentServiceTest {

    @Mock
    private NpssEnrolmentRepository npssEnrolmentRepository;
    @InjectMocks
    private NpssEnrolmentService npssEnrolmentService;

    @Test
    public void testEnrolmentEnrolled() {
        RequestMetaData metaData = getMetaData("012960010");
        NpssEnrolmentRepo dbResult = new NpssEnrolmentRepo();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepo> npssUser = Optional.of(digitalUser);
        Mockito.when(npssEnrolmentRepository.getEnrolmentStatus(Mockito.any())).thenReturn(npssUser);
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
