package ru.practicum.ewm.events.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.events.model.*;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;

public final class EventSpecifications {

    public static Specification<Event> stateIn(List<EventState> states) {
        return (root, query, cb) -> {
            if (states == null || states.isEmpty()) return cb.conjunction();
            return root.get("state").in(states);
        };
    }

    public static Specification<Event> initiatorIn(List<Long> userIds) {
        return (root, query, cb) -> {
            if (userIds == null || userIds.isEmpty()) return cb.conjunction();
            return root.get("initiator").get("id").in(userIds);
        };
    }

    public static Specification<Event> categoryIn(List<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) return cb.conjunction();
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Event> paid(Boolean paid) {
        return (root, query, cb) -> paid == null ? cb.conjunction() : cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime start) {
        return (root, query, cb) -> start == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    public static Specification<Event> eventDateBefore(LocalDateTime end) {
        return (root, query, cb) -> end == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    public static Specification<Event> text(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) return cb.conjunction();
            String like = "%" + text.toLowerCase() + "%";
            Expression<String> ann = cb.lower(root.get("annotation"));
            Expression<String> desc = cb.lower(root.get("description"));
            return cb.or(cb.like(ann, like), cb.like(desc, like));
        };
    }


//onlyAvailable=true:
//participantLimit == 0  => доступно всегда
//иначе confirmedRequests < participantLimit
    public static Specification<Event> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, cb) -> {
            if (onlyAvailable == null || !onlyAvailable) return cb.conjunction();

            Predicate unlimited = cb.equal(root.get("participantLimit"), 0);

            Subquery<Long> sub = query.subquery(Long.class);
            Root<ParticipationRequest> reqRoot = sub.from(ParticipationRequest.class);
            sub.select(cb.count(reqRoot.get("id")));
            sub.where(
                    cb.equal(reqRoot.get("event").get("id"), root.get("id")),
                    cb.equal(reqRoot.get("status"), RequestStatus.CONFIRMED)
            );

            Predicate limitNotReached = cb.greaterThan(root.get("participantLimit"), sub);
            return cb.or(unlimited, limitNotReached);
        };
    }
}
