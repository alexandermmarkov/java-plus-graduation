package ru.practicum.ewm.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.filter.StatsFilter;
import ru.practicum.ewm.mapper.EndpointHitMapper;
import ru.practicum.ewm.repository.EndpointHitRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class EndpointHitService {
    private final EndpointHitMapper mapper;
    private final EndpointHitRepository repository;

    @Transactional
    public void createHit(@NotNull(message = "Данные не получены или пустые") @Valid EndpointHitDto dto) {
        log.info("Создать запись hit (старт). uri: {}", dto.getUri());
        var entity = mapper.toEntity(dto);
        repository.save(entity);
        log.info("Создать запись hit (стоп). uri: {}", dto.getUri());
    }

    @Transactional(readOnly = true)
    public List<ViewStatsDto> findStats(@Valid StatsFilter filter) {
        log.info("Получить запись статистки (старт). filter: {}", filter);
        var result = filter.getUnique() ? repository.findStatsByUnique(filter) : repository.findStatsByNonUnique(filter);
        log.info("Получить запись статистки (стоп). filter: {}; записей в ответе: {}", filter, result.size());
        return result;
    }
}
