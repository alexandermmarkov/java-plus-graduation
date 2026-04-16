package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.event.EventFullDto;

@Slf4j
@Component
public class EventClientFallback implements EventClient {
    @Override
    public EventFullDto getEventById(Long id) {
        log.info("вызван EventClientFallback getEventById");
        return EventFullDto.builder()
                .id(id)
                .annotation("Мероприятие недоступно")
                .title("Мероприятие недоступно")
                .description("Невозможно загрузить данные о мероприятии")
                .build();
    }
}
