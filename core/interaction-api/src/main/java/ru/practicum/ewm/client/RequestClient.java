package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.constant.RequestStatus;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", fallback = RequestClientFallBack.class)
public interface RequestClient {
    @GetMapping("/requests/count/events/{eventId}/{status}")
    Long getCountRequestsByEventIdAndStatus(@PathVariable("eventId") Long eventId,
                                            @PathVariable("status") RequestStatus status);

    @GetMapping("/requests/count/events/batch")
    Map<Long, Long> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds);

    @RequestMapping(value = "/requests/participation/{userId}/{eventId}")
    Boolean checkUserParticipation(@PathVariable Long userId, @PathVariable Long eventId);
}
