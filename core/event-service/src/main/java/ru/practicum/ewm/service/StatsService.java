package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsClient statsClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Long getViewsForEvent(Long eventId) {
        String start = LocalDateTime.now().minusDays(1).format(formatter);
        String end = LocalDateTime.now().plusDays(1).format(formatter);

        List<String> uris = List.of("/events/" + eventId);
        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris.toArray(new String[0]), true);
        return stats.isEmpty() ? 0L : stats.getFirst().getHits();
    }


    public Map<String, Long> getViewsForUris(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Map.of();
        }

        String start = LocalDateTime.now().minusYears(20L).format(formatter);
        String end = LocalDateTime.now().format(formatter);

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris.toArray(new String[0]), true);

        return stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits,
                        (existing, replacement) -> existing // на случай дублей
                ));
    }

    public void saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        try {
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app(app)
                    .uri(uri)
                    .ip(ip)
                    .timestamp(timestamp)
                    .build();

            statsClient.saveHit(hitDto);
            log.info("Сохранён хит статистики: app={}, uri={}, ip={}", app, uri, ip);
        } catch (Exception e) {
            log.error("Не удалось сохранить статистику для uri {}: {}", uri, e.getMessage(), e);
        }
    }
}
