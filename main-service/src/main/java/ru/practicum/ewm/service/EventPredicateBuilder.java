package ru.practicum.ewm.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.util.StringUtils;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.filter.EventsFilter;
import ru.practicum.ewm.model.QEvent;

import java.time.LocalDateTime;

public class EventPredicateBuilder {

    public static BooleanBuilder buildPredicate(EventsFilter filter, boolean forAdmin) {
        QEvent event = QEvent.event;
        BooleanBuilder predicate = new BooleanBuilder();
        if (!forAdmin) {
            predicate.and(event.state.eq(EventState.PUBLISHED));
        }

        if (filter.getRangeStart() != null) {
            predicate.and(event.eventDate.goe(filter.getRangeStart()));
        } else if (!forAdmin) {
            predicate.and(event.eventDate.gt(LocalDateTime.now()));
        }

        if (filter.getRangeEnd() != null) {
            predicate.and(event.eventDate.loe(filter.getRangeEnd()));
        }

        if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
            predicate.and(event.category.id.in(filter.getCategories()));
        }

        if (!forAdmin && filter.getPaid() != null) {
            predicate.and(event.paid.eq(filter.getPaid()));
        }

        if (!forAdmin && StringUtils.hasText(filter.getText())) {
            String searchText = "%" + filter.getText().toLowerCase() + "%";
            predicate.and(
                    event.annotation.likeIgnoreCase(searchText)
                            .or(event.description.likeIgnoreCase(searchText))
            );
        }

        if (!forAdmin && filter.getOnlyAvailable() != null && filter.getOnlyAvailable()) {
            predicate.and(event.participantLimit.eq(0L)
                    .or(event.participantLimit.gt(10L)));
        }

        if (forAdmin && filter.getUsers() != null && !filter.getUsers().isEmpty()) {
            predicate.and(event.initiator.id.in(filter.getUsers()));
        }

        if (forAdmin && filter.getStates() != null && !filter.getStates().isEmpty()) {
            var states = filter.getStatesAsEnum();
            predicate.and(event.state.in(states));
        }
        return predicate;
    }
}