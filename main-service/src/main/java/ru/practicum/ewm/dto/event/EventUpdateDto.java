package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import ru.practicum.ewm.constant.EventStateAction;
import ru.practicum.ewm.core.annotation.DateRange;
import ru.practicum.ewm.core.annotation.DateRangeAware;
import ru.practicum.ewm.core.annotation.FutureBy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DateRange
public class EventUpdateDto implements DateRangeAware {
    private Long id;

    private Long userId;

    @Nullable
    @Size(min = 20, max = 2000, message = "Количество символов в annotation от 20 до 2000")
    private String annotation;

    private Long category;

    @Nullable
    @Size(min = 20, max = 7000, message = "Количество символов в description от 20 до 7000")
    private String description;

    @Nullable
    @FutureBy(
            amount = 2,
            unit = ChronoUnit.HOURS,
            message = "eventDate не может быть раньше текущего времени минимум на 2 часа"
    )
    private LocalDateTime eventDate;

    private EventLocationDto location;

    private Boolean paid;

    @Nullable
    @PositiveOrZero(message = "participantLimit >= 0")
    private Long participantLimit;

    private Boolean requestModeration;

    private EventStateAction stateAction;

    @Nullable
    @Size(min = 3, max = 120, message = "Количество символов в title от 3 до 120")
    private String title;

    @Override
    public LocalDateTime getStart() {
        return null;
    }

    @Override
    public LocalDateTime getEnd() {
        return null;
    }
}
