package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    Page<Event> findAllByInitiatorId(Long userId, Pageable page);

    Boolean existsByCategoryId(Long categoryId);

    @Query("SELECT ev FROM Event ev WHERE ev.id IN :eventIds")
    List<Event> findEventsByEventIds(@Param("eventIds") List<Long> eventIds);
}
