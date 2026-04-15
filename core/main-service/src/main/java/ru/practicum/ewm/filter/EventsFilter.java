package ru.practicum.ewm.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.core.annotation.DateRange;
import ru.practicum.ewm.core.annotation.DateRangeAware;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

@DateRange
public class EventsFilter implements DateRangeAware {

    private String text;

    private Boolean paid;

    // --- Добавлено из AdminEventsFilter ---
    private List<Long> users;
    private List<String> states;
    // ---------------------------------------

    private List<Long> categories;

    //@NotNull(message = "фильтр: `Время запроса начало` обязательно к заполнению")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    //@NotNull(message = "фильтр: `Время запроса окончание` обязательно к заполнению")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable = false;

    @Override
    public LocalDateTime getStart() {
        return rangeStart;
    }

    @Override
    public LocalDateTime getEnd() {
        return rangeEnd;
    }

    // Утилитный метод для получения states как EventState (чтобы не дублировать в сервисе)
    public List<EventState> getStatesAsEnum() {
        if (this.states == null || this.states.isEmpty()) {
            return null;
        }
        return this.states.stream()
                .map(EventState::valueOf)
                .toList();
    }
}