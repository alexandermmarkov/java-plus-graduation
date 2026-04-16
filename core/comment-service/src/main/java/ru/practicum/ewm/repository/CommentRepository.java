package ru.practicum.ewm.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.eventId = :eventId AND c.deleted = false ORDER BY c.created DESC")
    Optional<List<Comment>> findCommentsByEvent(@Param("eventId") Long eventId);

    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.deleted = false")
    Optional<Comment> findByIdAndDeletedFalse(@Param("id") Long id);

    List<Comment> findByEventIdIn(List<Long> eventIds);
}
