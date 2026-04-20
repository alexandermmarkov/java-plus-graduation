package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.UserAction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT ua.eventId FROM UserAction ua " +
            "WHERE ua.userId = :userId " +
            "ORDER BY ua.timestamp DESC")
    List<Long> findRecentEventIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ua.eventId FROM UserAction ua WHERE ua.userId = :userId")
    Set<Long> findEventIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT ua FROM UserAction ua WHERE (ua.eventId IN :eventIds)")
    List<UserAction> findActionsForListEventIds(@Param("eventIds") List<Long> eventIds);
}
