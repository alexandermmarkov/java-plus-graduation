package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.constant.RequestStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RequestClientFallBack implements RequestClient {
    @Override
    public Long getCountRequestsByEventIdAndStatus(Long eventId, RequestStatus status) {
        log.info("вызван RequestClientFallBack getCountRequestsByEventIdAndStatus");
        return 0L;
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds) {
        log.info("вызван RequestClientFallBack getConfirmedRequestsForEvents");
        return new HashMap<>();
    }
}
