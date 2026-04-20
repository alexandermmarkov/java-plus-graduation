package ru.practicum.ewm;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.service.EventSimilarityProcessor;
import ru.practicum.ewm.service.UserActionProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements ApplicationRunner {

    private final UserActionProcessor userActionProcessor;
    private final EventSimilarityProcessor eventSimilarityProcessor;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable ->
            new Thread(runnable, "HubEventHandlerThread")
    );

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        executor.execute(userActionProcessor);
        eventSimilarityProcessor.run();
    }

    @PreDestroy
    public void shutdown() {
        log.info("shutdownNow HubEventProcessor executor...");
        executor.shutdownNow();
    }
}
