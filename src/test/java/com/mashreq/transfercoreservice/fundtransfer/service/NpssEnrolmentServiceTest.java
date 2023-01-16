package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import com.mashreq.transfercoreservice.repository.NpssEnrolmentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
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
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepoDTO> npssUser = Optional.of(dbResult);
        Mockito.when(npssEnrolmentRepository.getEnrolmentStatus(Mockito.any())).thenReturn(npssUser);
        npssEnrolmentService.checkEnrolment(metaData);
    }

/*    @Test
    public void testEnrolmentWithSuccess() {
        RequestMetaData metaData = getMetaData("012960010");
        List<NpssEnrolmentRepoDTO> npssEnrolmentResponse = new ArrayList<>();
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepoDTO> npssUser = Optional.of(dbResult);
        Mockito.when(npssEnrolmentRepository.findAllByIsDefaultAccountUpdated(Mockito.anyBoolean())).thenReturn(npssEnrolmentResponse);
        Mockito.when(npssEnrolmentRepository.save(Mockito.any())).thenReturn(npssUser);
        npssEnrolmentService.updateEnrolment(metaData);
    }*/

    private RequestMetaData getMetaData(String cif) {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif(cif);
        return metaData;
    }
}
