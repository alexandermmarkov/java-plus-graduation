package ru.practicum.ewm.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.CommentUpdateDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentFacadeService {
    private final UserClient userClient;
    private final CommentMapper commentMapper;
    private final CommentService commentService;

    public CommentDto findById(Long id) {
        log.info("Найти комментарий {}", id);
        Comment comment = commentService.getCommentById(id);
        Long authorId = comment.getAuthorId();
        UserDto userDto = getUserOrThrow(authorId);
        return commentService.getCommentDto(comment, userDto);
    }

    public CommentDto create(CommentUpdateDto entity, Long userId) {
        log.info("Создать комментарий {}: {}", userId, entity);
        UserDto userDto = getUserOrThrow(userId);

        return commentService.create(entity, userId, userDto);
    }

    public CommentDto update(CommentUpdateDto dto, Long commentId, Long userId) throws ConditionsException {
        log.info("Изменить комментарий: userId {}; commentId {}; dto {}", userId, commentId, dto);
        UserDto userDto = getUserOrThrow(userId);
        return commentService.update(dto, commentId, userId, userDto);
    }

    public void delete(Long commentId, Long userId) throws ConditionsException {
        log.info("Удалить комментарий: userId {}; commentId {}", userId, commentId);
        UserDto userDto = getUserOrThrow(userId);
        CommentUpdateDto dto = CommentUpdateDto.builder().deleted(true).build();
        commentService.update(dto, commentId, userId, userDto);
    }


    public void deleteAdmin(Long commentId) throws ConditionsException {
        log.info("Удалить комментарий {}", commentId);
        Comment comment = commentService.getCommentById(commentId);
        Long authorId = comment.getAuthorId();
        UserDto userDto = getUserOrThrow(authorId);
        CommentUpdateDto dto = CommentUpdateDto.builder()
                .deleted(true)
                .isAdmin(true)
                .build();
        commentService.update(dto, commentId, authorId, userDto);
    }

    public List<CommentDto> findAllCommentsForEvent(Long eventId) {
        log.info("Получаем комментарии по событию с id={}", eventId);
        List<Comment> comments = commentService.findCommentsByEvent(eventId);
        if (comments.isEmpty()) {
            return List.of();
        }
        // Список id авторов, которые комментировали событие
        List<Long> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .distinct()
                .toList();
        // Список юзеров по списку id авторов
        List<UserDto> users = userClient.getUsers(authorIds);
        // связь authorId → UserDto
        Map<Long, UserDto> userMap = users.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        log.info("Возвращаем {} комментариев события с id={}", comments.size(), eventId);
        return comments.stream()
                .map(comment -> {
                    CommentDto dto = commentMapper.toDto(comment);
                    dto.setAuthorName(userMap.get(comment.getAuthorId()).getName());
                    return dto;
                })
                .toList();
    }

    public Map<Long, List<CommentDto>> getCommentsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Comment> comments = commentService.getCommentsForEvents(eventIds);

        return comments.stream()
                .collect(Collectors.groupingBy(
                        Comment::getEventId,
                        Collectors.mapping(commentMapper::toDto, Collectors.toList())
                ));

    }


    private UserDto getUserOrThrow(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
