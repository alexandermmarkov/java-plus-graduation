package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.CommentUpdateDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;


    public CommentDto getCommentDto(Comment comment, UserDto userDto) {
        return commentMapper.toDto(comment).toBuilder()
                .authorName(userDto.getName())
                .build();
    }

    public Comment getCommentById(Long id) {
        return getCommentOrThrow(id);
    }

    @Transactional
    public CommentDto create(CommentUpdateDto entity, Long userId, UserDto userDto) {
        log.info("Создать комментарий {}: {}", userId, entity);
        Comment comment = Comment.builder()
                .authorId(userId)
                .eventId(entity.getEventId())
                .text(entity.getText())
                .deleted(false)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);
        log.info("Создание нового комментария к событию с id={} пользователем с id={}",
                entity.getEventId(), userId);
        return commentMapper.toDto(comment).toBuilder()
                .authorName(userDto.getName())
                .build();
    }

    @Transactional
    public CommentDto update(CommentUpdateDto dto, Long commentId, Long userId, UserDto userDto) throws ConditionsException {
        log.info("Изменить комментарий: userId {}; commentId {}; dto {}", userId, commentId, dto);

        Comment comment = getCommentOrThrow(commentId);
        if (!dto.getIsAdmin() && !isAuthorComment(comment.getAuthorId(), userId)) {
            throw new ConditionsException("Вы не можете редактировать данный комментарий");
        }
        comment = commentMapper.mapEntityFromDto(comment, dto);
        comment = commentRepository.save(comment);
        log.info("Обновление комментария к событию с id={} пользователем с id {}",
                comment.getEventId(), userId);
        return commentMapper.toDto(comment).toBuilder()
                .authorName(userDto.getName())
                .build();
    }


    @Transactional(readOnly = true)
    public List<Comment> getCommentsForEvents(List<Long> eventIds) {
        return commentRepository.findByEventIdIn(eventIds);
    }

    private boolean isAuthorComment(Long authorId, Long userId) {
        if (userId == null || authorId == null) {
            return false;
        }
        return Objects.equals(authorId, userId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findCommentsByEvent(Long eventId) {
        return commentRepository.findCommentsByEvent(eventId).orElse(List.of());
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден."));
    }

}
