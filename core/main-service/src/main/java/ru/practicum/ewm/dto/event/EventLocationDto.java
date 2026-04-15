package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLocationDto {
    @NotNull
    private Float lat;

    @NotNull
    private Float lon;
}
