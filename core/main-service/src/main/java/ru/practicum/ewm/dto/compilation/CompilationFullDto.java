package ru.practicum.ewm.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.dto.event.EventShortDto;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationFullDto extends CompilationCommonDto {

    private List<EventShortDto> events;
}
