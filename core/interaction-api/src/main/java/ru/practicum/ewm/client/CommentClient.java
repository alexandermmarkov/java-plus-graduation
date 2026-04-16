package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.comment.CommentDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "comment-service", fallback = CommentClientFallback.class)
public interface CommentClient {

    @GetMapping("/comments/event/{eventId}")
    List<CommentDto> findAllCommentsForEvent(@PathVariable("eventId") Long eventId);

    @GetMapping("/comments/events/batch")
    Map<Long, List<CommentDto>> getCommentsForEvents(@RequestParam List<Long> eventIds);
}
