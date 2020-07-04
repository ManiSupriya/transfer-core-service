package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.event.repository.UserEventAuditRepository;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/events")
public class EventController {

    private final UserEventAuditRepository userEventAuditRepository;

    @GetMapping("/loginId/{loginId}")
    public Response getEventsByLoginId(@PathVariable final String loginId) {


        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(userEventAuditRepository.findByUsername(loginId)
                        .stream()
                        .collect(Collectors.groupingBy(x -> x.getCorrelationId())))
                .build();
    }
}
