package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class DigitalUserServiceTest {

    @Mock
    private DigitalUserRepository digitalUserRepository;
    @InjectMocks
    private DigitalUserService digitalUserService;

    @Test
    public void testGetDigitalUser() {
        RequestMetaData metaData = getMetaData("012960010");
        DigitalUser digitalUser = new DigitalUser();
        digitalUser.setCif("012960010");
        digitalUser.setCountry("EN");
        Optional<DigitalUser> digitalUserOpr = Optional.of(digitalUser);
        Mockito.when(digitalUserRepository.findByCifEquals(Mockito.any())).thenReturn(digitalUserOpr);
        digitalUserService.getDigitalUser(metaData);
    }
    @Test(expected = Exception.class)
    public void testGetDigitalUserNull() {
        RequestMetaData metaData = getMetaData("012960010");
        DigitalUser digitalUser = new DigitalUser();
        digitalUser.setCif("012960010");
        digitalUser.setCountry("EN");
        Optional<DigitalUser> digitalUserOpr = Optional.of(null);
        Mockito.when(digitalUserRepository.findByCifEquals(Mockito.any())).thenReturn(digitalUserOpr);
        digitalUserService.getDigitalUser(metaData);
    }
    private RequestMetaData getMetaData(String cif) {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif(cif);
        return metaData;
    }
}
