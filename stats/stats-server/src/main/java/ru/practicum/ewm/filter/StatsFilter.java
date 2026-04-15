package ru.practicum.ewm.filter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.core.annotation.DateRange;
import ru.practicum.ewm.core.annotation.DateRangeAware;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

@DateRange
public class StatsFilter implements DateRangeAware {

    @NotNull(message = "фильтр: `Время запроса начало` обязательно к заполнению")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @NotNull(message = "фильтр: `Время запроса окончание` обязательно к заполнению")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    private List<String> uris;

    private Boolean unique = false;
}
