package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.comment.CommentDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CommentClientFallback implements CommentClient {
    public List<CommentDto> findAllCommentsForEvent(Long eventId) {
        log.info("вызван CommentClientFallback findAllCommentsForEvent");
        return List.of(CommentDto.builder()
                .id(-1L)
                .authorName("Сервис комментариев недоступен")
                .event(eventId)
                .text("Невозможно загрузить комментарии")
                .build());
    }
    public Map<Long, List<CommentDto>> getCommentsForEvents(@RequestParam List<Long> eventIds) {
        log.info("вызван CommentClientFallback getCommentsForEvents");
        return new HashMap<>();
    }
}
