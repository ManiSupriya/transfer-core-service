package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cardlesscash.repository.DigitalUserSegmentRepository;
import com.mashreq.transfercoreservice.cardlesscash.service.DigitalUserSegmentService;
import com.mashreq.transfercoreservice.model.Segment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DigitalUserSegmentServiceTest {

    @Mock
    private DigitalUserSegmentRepository digitalUserSegmentRepository;

    @InjectMocks
    private DigitalUserSegmentService digitalUserSegmentService;

    @Test
    public void testDigitalUserSegmentByName(){
        var segment = new Segment();
        segment.setName("GOLD");
        segment.setCustomerCareNumber("+971 123 4567");
        segment.setLocalContactNumber("+971 456 789");

        when(digitalUserSegmentRepository.findByName("GOLD")).thenReturn(Optional.of(segment));

        var response = digitalUserSegmentService.getDigitalUserSegmentByName("GOLD");

        assertEquals("GOLD", response.getName());
        assertEquals("+971 123 4567", response.getCustomerCareNumber());
    }

    @Test
    public void testDigitalUserSegmentByNameSegmentNotFound(){

        when(digitalUserSegmentRepository.findByName("ABC")).thenReturn(null);

       var exception = assertThrows(GenericException.class, ()->{
            digitalUserSegmentService.getDigitalUserSegmentByName("GOLD");
        });

       assertEquals("CS-1012", exception.getErrorCode());

    }
}
