package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.constant.RequestStatus;
import ru.practicum.ewm.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/count/events/{eventId}/{status}")
    public Long getCountRequestsByEventIdAndStatus(@Positive @PathVariable Long eventId,
                                                   @PathVariable RequestStatus status) {
        log.info("Получить количество принятых заявок на мероприятие {}", eventId);
        return requestService.getCountRequestsByEventIdAndStatus(eventId, status);
    }

    @GetMapping("/count/events/batch")
    public Map<Long, Long> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds) {
        log.info("Получить количество принятых заявок по каждому мероприятию из списка {}", eventIds);
        return requestService.getConfirmedRequestsForEvents(eventIds);
    }

}
