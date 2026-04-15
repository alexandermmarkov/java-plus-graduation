package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.core.annotation.FutureBy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventNewDto {

    @NotBlank(message = "Не заполнено поле annotation")
    @Size(min = 20, max = 2000, message = "Количество символов в annotation от 20 до 2000")
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank(message = "Не заполнено поле description")
    @Size(min = 20, max = 7000, message = "Количество символов в description от 20 до 7000")
    private String description;

    @NotNull(message = "Не заполнено поле eventDate")
    @FutureBy(
            amount = 2,
            unit = ChronoUnit.HOURS,
            message = "eventDate не может быть раньше текущего времени минимум на 2 часа"
    )
    private LocalDateTime eventDate;

    @NotNull
    private EventLocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @Builder.Default
    @PositiveOrZero(message = "participantLimit >= 0")
    private Long participantLimit = 0L;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Не заполнено поле title")
    @Size(min = 3, max = 120, message = "Количество символов в title от 3 до 120")
    private String title;

    private EventState state = EventState.PENDING;

    private LocalDateTime publishedOn = null;
}
