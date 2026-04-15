package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.ViewStatsDto;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsClient {

    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;
    private static final String STATS_SERVICE_ID = "stats-server";

    private String getBaseUrl() {
        try {
            ServiceInstance instance = discoveryClient
                    .getInstances(STATS_SERVICE_ID)
                    .getFirst();
            return instance.getUri().toString();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Сервис статистики недоступен: " + STATS_SERVICE_ID, e
            );
        }
    }

    public void saveHit(EndpointHitDto hitDto) {
        String baseUrl = getBaseUrl();
        String url = baseUrl + "/hit";
        log.info("Отправка запроса saveHit: url={}, body={}", url, hitDto);

        restClient.post()
                .uri(url)
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();

        log.info("Hit был сохранен");
    }

    public List<ViewStatsDto> getStats(String start, String end, String[] uris, boolean unique) {

        log.info("вошли в getStats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        String baseUrl = getBaseUrl();
        String startEncoded = start.replace(" ", "+");
        String endEncoded = end.replace(" ", "+");
        URI fullUrl = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", startEncoded)
                .queryParam("end", endEncoded)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .build(true)
                .toUri();

        log.info("Отправка запроса getStats: url={}", fullUrl);

        List<ViewStatsDto> stats = Arrays.asList(
                Objects.requireNonNull(restClient.get()
                        .uri(fullUrl)
                        .retrieve()
                        .body(ViewStatsDto[].class))
        );

        log.info("getStats вернул {} записей", stats.size());
        return stats;
    }
}
