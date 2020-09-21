package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.repository.SegmentMsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class DigitalUserSegment  {

    @Autowired
    SegmentMsRepository segmentMsRepository;
    private Map<String, Segment> customerCareInfoMap;

    public Segment getCustomerCareInfo(String segment){
        if (customerCareInfoMap != null && customerCareInfoMap.containsKey(segment)) {
            return customerCareInfoMap.get(segment);
        }

        customerCareInfoMap = new HashMap<>();
        segmentMsRepository.findAll()
                .forEach(segmentInfo -> customerCareInfoMap.put(segmentInfo.getName(), segmentInfo));

        return customerCareInfoMap.getOrDefault(segment, new Segment());
    }

}