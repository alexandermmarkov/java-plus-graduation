package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.constant.RequestStatus;
import ru.practicum.ewm.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT r.eventId, COUNT(r) " +
            "FROM Request r " +
            "WHERE r.eventId IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.eventId")
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByIdInAndStatus(List<Long> ids, RequestStatus status);

    Boolean existsByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, RequestStatus status);
}
