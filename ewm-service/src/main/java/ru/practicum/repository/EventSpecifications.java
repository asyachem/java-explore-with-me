package ru.practicum.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.enums.EventState;
import ru.practicum.model.Event;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class EventSpecifications {
    public static Specification<Event> byAdminFilters(List<Long> users,
                                                      List<EventState> states,
                                                      List<Long> categories,
                                                      LocalDateTime start,
                                                      LocalDateTime end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }

            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> publicEvents(String text,
                                                    List<Long> categories,
                                                    Boolean paid,
                                                    LocalDateTime rangeStart,
                                                    LocalDateTime rangeEnd,
                                                    Boolean onlyAvailable) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

            if (text != null && !text.isBlank()) {
                Predicate annotationPredicate = cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%");
                predicates.add(cb.or(annotationPredicate, descriptionPredicate));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (paid != null) {
                predicates.add(cb.equal(root.get("paid"), paid));
            }

            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));

            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            if (Boolean.TRUE.equals(onlyAvailable)) {
                Predicate noLimit = cb.equal(root.get("participantLimit"), 0);
                Predicate notFull = cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit"));
                predicates.add(cb.or(noLimit, notFull));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
