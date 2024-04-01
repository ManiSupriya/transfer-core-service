package com.mashreq.transfercoreservice.cardlesscash.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cardlesscash.repository.DigitalUserSegmentRepository;
import com.mashreq.transfercoreservice.model.Segment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mashreq.transfercoreservice.cardlesscash.constants.CardlessCashErrorCode.SEGMENT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalUserSegmentService {

    private final DigitalUserSegmentRepository digitalUserSegmentRepository;
    
    public Segment getDigitalUserSegmentByName(String name) {
        var segment = digitalUserSegmentRepository.findByName(name);
        if (segment.isEmpty()) {
            GenericExceptionHandler.handleError(SEGMENT_NOT_FOUND, SEGMENT_NOT_FOUND.getMessage());
        }
        return segment.get();
    }

}
