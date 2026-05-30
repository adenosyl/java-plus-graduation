package ru.practicum.ewm.events.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.mapper.RequestMapper;
import ru.practicum.ewm.events.model.*;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.events.repository.EventSpecifications;
import ru.practicum.ewm.events.repository.ParticipationRequestRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.events.util.DateTimeUtil;
import ru.practicum.ewm.events.util.OffsetBasedPageRequest;

import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import ru.practicum.ewm.users.repository.UserRepository;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.category.model.Category;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private final StatsFacade statsFacade;

    //PUBLIC

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               PublicEventSort sort,
                                               int from,
                                               int size,
                                               HttpServletRequest request) {

        statsFacade.hit(request);

        LocalDateTime start = DateTimeUtil.parseNullable(rangeStart);
        LocalDateTime end = DateTimeUtil.parseNullable(rangeEnd);

        // если диапазон не задан — только будущие события
        if (start == null && end == null) {
            start = LocalDateTime.now();
        } else if (start == null) {
            start = LocalDateTime.now();
        }

        if (end != null && start != null && end.isBefore(start)) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала!");
        }

        Specification<Event> spec = Specification.where(EventSpecifications.stateIn(List.of(EventState.PUBLISHED)))
                .and(EventSpecifications.text(text))
                .and(EventSpecifications.categoryIn(categories))
                .and(EventSpecifications.paid(paid))
                .and(EventSpecifications.eventDateAfter(start))
                .and(EventSpecifications.eventDateBefore(end))
                .and(EventSpecifications.onlyAvailable(onlyAvailable));

        if (sort == PublicEventSort.VIEWS) {
            List<Event> all = eventRepository.findAll(spec);
            List<EventShortDto> mapped = toShortDtosWithMeta(all);
            mapped.sort(
                    Comparator.comparingLong((EventShortDto d) -> d.getViews() == null ? 0L : d.getViews())
                            .reversed()
            );

            return slice(mapped, from, size);
        }

        // EVENT_DATE (или null): сортируем по eventDate в БД
        Sort dbSort = Sort.by(Sort.Direction.ASC, "eventDate");
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(from, size, dbSort);

        List<Event> page = eventRepository.findAll(spec, pageable).getContent();
        return toShortDtosWithMeta(page);
    }

    @Override
    public EventFullDto getPublicEvent(long id, HttpServletRequest request) {
        statsFacade.hit(request);

        Event event = eventRepository.findById(id)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие в id= " + id + " не найдено!"));

        return toFullDtoWithMeta(event);
    }

    //PRIVATE

    @Override
    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        ensureUserExists(userId);

        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(from, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("initiator").get("id"), userId),
                pageable
        ).getContent();

        return toShortDtosWithMeta(events);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден!"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + userId + " не найдена!"));

        // правило Swagger: не раньше чем через 2 часа
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("До даты события должно оставаться не менее 2 часов!");
        }

        Event e = new Event();
        e.setTitle(dto.getTitle());
        e.setAnnotation(dto.getAnnotation());
        e.setDescription(dto.getDescription());
        e.setCategory(category);
        e.setInitiator(user);
        e.setLocation(EventMapper.toEmb(dto.getLocation()));
        e.setEventDate(dto.getEventDate());

        e.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        e.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        e.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

        e.setState(EventState.PENDING);
        e.setCreatedOn(LocalDateTime.now());
        e.setPublishedOn(null);

        Event saved = eventRepository.save(e);

        EventFullDto out = toFullDtoWithMeta(saved);
        out.setViews(0L);
        out.setConfirmedRequests(0L);
        return out;
    }

    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        ensureUserExists(userId);

        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + userId + " не найдено!"));

        if (!Objects.equals(e.getInitiator().getId(), userId)) {
            throw new NotFoundException("Событие с id=" + userId + " не найдено!");
        }

        return toFullDtoWithMeta(e);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest dto) {
        ensureUserExists(userId);

        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + userId + " не найдено!"));

        if (!Objects.equals(e.getInitiator().getId(), userId)) {
            throw new NotFoundException("Событие с id=" + userId + " не найдено!");
        }

        // правило Swagger: менять можно только CANCELED или PENDING
        if (!(e.getState() == EventState.PENDING || e.getState() == EventState.CANCELED)) {
            throw new ConflictException("Допускается изменение событий, находящихся в статусе pending или canceled.");
        }

        // правило Swagger: дата не раньше +2 часа
        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("До даты события должно оставаться не менее 2 часов");
        }

        applyUserUpdate(e, dto);

        return toFullDtoWithMeta(eventRepository.save(e));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(long userId, long eventId) {
        ensureUserExists(userId);

        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + userId + " не найдено!"));

        if (!Objects.equals(e.getInitiator().getId(), userId)) {
            throw new NotFoundException("Событие с id=" + userId + " не найдено!");
        }

        return requestRepository.findAllByEvent_IdOrderByIdAsc(eventId)
                .stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(long userId, long eventId, EventRequestStatusUpdateRequest dto) {
        ensureUserExists(userId);

        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + userId + " не найдено!"));

        if (!Objects.equals(e.getInitiator().getId(), userId)) {
            throw new NotFoundException("Событие с id=" + userId + " не найдено!");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(dto.getRequestIds());
        if (requests.size() != dto.getRequestIds().size()) {
            throw new NotFoundException("Не все запросы были найдены.");
        }

        for (ParticipationRequest r : requests) {
            if (!Objects.equals(r.getEvent().getId(), eventId)) {
                throw new ConflictException("Запрос не принадлежит указанному событию!");
            }
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Изменение статуса допускается исключительно для запросов в статусе PENDING!");
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        // если лимит 0 или модерация выключена — подтверждение не требуется
        if (e.getParticipantLimit() == 0 || !Boolean.TRUE.equals(e.getRequestModeration())) {
            for (ParticipationRequest r : requests) {
                if (dto.getStatus() == RequestUpdateStatus.CONFIRMED) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    result.getConfirmedRequests().add(RequestMapper.toDto(r));
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(RequestMapper.toDto(r));
                }
            }
            requestRepository.saveAll(requests);
            return result;
        }

        long confirmed = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = e.getParticipantLimit();

        if (dto.getStatus() == RequestUpdateStatus.CONFIRMED) {
            for (ParticipationRequest r : requests) {
                if (confirmed >= limit) {
                    throw new ConflictException("Превышено допустимое количество участников!");
                }
                r.setStatus(RequestStatus.CONFIRMED);
                confirmed++;
                result.getConfirmedRequests().add(RequestMapper.toDto(r));
            }
            requestRepository.saveAll(requests);

            // если лимит исчерпан — отклонить все оставшиеся pending
            if (confirmed >= limit) {
                List<ParticipationRequest> pending = requestRepository.findAllByEvent_IdAndStatus(eventId, RequestStatus.PENDING);
                for (ParticipationRequest p : pending) {
                    p.setStatus(RequestStatus.REJECTED);
                }
                requestRepository.saveAll(pending);
            }

            return result;
        }

        // REJECTED
        for (ParticipationRequest r : requests) {
            r.setStatus(RequestStatus.REJECTED);
            result.getRejectedRequests().add(RequestMapper.toDto(r));
        }
        requestRepository.saveAll(requests);
        return result;
    }

    //ADMIN

    @Override
    public List<EventFullDto> searchAdmin(List<Long> users,
                                          List<EventState> states,
                                          List<Long> categories,
                                          String rangeStart,
                                          String rangeEnd,
                                          int from,
                                          int size) {

        LocalDateTime start = DateTimeUtil.parseNullable(rangeStart);
        LocalDateTime end = DateTimeUtil.parseNullable(rangeEnd);

        Specification<Event> spec = Specification.where(EventSpecifications.initiatorIn(users))
                .and(EventSpecifications.stateIn(states))
                .and(EventSpecifications.categoryIn(categories))
                .and(EventSpecifications.eventDateAfter(start))
                .and(EventSpecifications.eventDateBefore(end));

        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(from, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        return toFullDtosWithMeta(events);
    }

    @Override
    @Transactional
    public EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest dto) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено!"));
        applyAdminUpdate(e, dto);

        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата событие не может быть в прошлом!");
        }

        return toFullDtoWithMeta(eventRepository.save(e));
    }

    //helpers

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден!");
        }
    }

    private void applyUserUpdate(Event e, UpdateEventUserRequest dto) {
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) e.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) e.setLocation(EventMapper.toEmb(dto.getLocation()));
        if (dto.getPaid() != null) e.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) e.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) e.setRequestModeration(dto.getRequestModeration());

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена!"));
            e.setCategory(category);
        }

        if (dto.getStateAction() != null) {
            if (dto.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                e.setState(EventState.PENDING);
            } else if (dto.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                e.setState(EventState.CANCELED);
            }
        }
    }

    private void applyAdminUpdate(Event e, UpdateEventAdminRequest dto) {
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) e.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) e.setLocation(EventMapper.toEmb(dto.getLocation()));
        if (dto.getPaid() != null) e.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) e.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) e.setRequestModeration(dto.getRequestModeration());

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена!"));
            e.setCategory(category);
        }

        if (dto.getStateAction() != null) {
            if (dto.getStateAction() == AdminStateAction.PUBLISH_EVENT) {

                if (e.getState() != EventState.PENDING) {
                    throw new ConflictException("Статус события должен быть PENDING для его публикации!");
                }

                if (e.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Опубликовать событие можно, только если до его даты остаётся не менее 1 часа.");
                }
                e.setState(EventState.PUBLISHED);
                e.setPublishedOn(LocalDateTime.now());
            } else if (dto.getStateAction() == AdminStateAction.REJECT_EVENT) {

                if (e.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Отклонить событие разрешается только до его публикации.");
                }
                e.setState(EventState.CANCELED);
            }
        }
    }

    private List<EventShortDto> toShortDtosWithMeta(List<Event> events) {
        if (events.isEmpty()) return List.of();

        Map<Long, Long> confirmed = getConfirmedMap(events);
        Map<Long, Long> views = getViewsMap(events);

        return events.stream().map(e -> {
            EventShortDto dto = EventMapper.toShortDto(e);

            dto.setCategory(mapCategory(e.getCategory()));
            dto.setInitiator(mapInitiator(e.getInitiator()));

            dto.setConfirmedRequests(confirmed.getOrDefault(e.getId(), 0L));
            dto.setViews(views.getOrDefault(e.getId(), 0L));
            return dto;
        }).toList();
    }

    private List<EventFullDto> toFullDtosWithMeta(List<Event> events) {
        if (events.isEmpty()) return List.of();

        Map<Long, Long> confirmed = getConfirmedMap(events);
        Map<Long, Long> views = getViewsMap(events);

        return events.stream().map(e -> {
            EventFullDto dto = EventMapper.toFullDto(e);

            dto.setCategory(mapCategory(e.getCategory()));
            dto.setInitiator(mapInitiator(e.getInitiator()));

            dto.setConfirmedRequests(confirmed.getOrDefault(e.getId(), 0L));
            dto.setViews(views.getOrDefault(e.getId(), 0L));
            return dto;
        }).toList();
    }

    private EventFullDto toFullDtoWithMeta(Event e) {
        Map<Long, Long> confirmed = getConfirmedMap(List.of(e));
        Map<Long, Long> views = getViewsMap(List.of(e));

        EventFullDto dto = EventMapper.toFullDto(e);
        dto.setCategory(mapCategory(e.getCategory()));
        dto.setInitiator(mapInitiator(e.getInitiator()));
        dto.setConfirmedRequests(confirmed.getOrDefault(e.getId(), 0L));
        dto.setViews(views.getOrDefault(e.getId(), 0L));
        return dto;
    }

    private Map<Long, Long> getConfirmedMap(List<Event> events) {
        List<Long> ids = events.stream().map(Event::getId).toList();
        List<Object[]> rows = requestRepository.countByEventIdsAndStatus(ids, RequestStatus.CONFIRMED);

        Map<Long, Long> result = new HashMap<>();
        for (Object[] r : rows) {
            Long eventId = (Long) r[0];
            Long cnt = (Long) r[1];
            result.put(eventId, cnt);
        }
        return result;
    }

    private Map<Long, Long> getViewsMap(List<Event> events) {
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        Map<String, Long> uriViews = statsFacade.getViews(uris);

        Map<Long, Long> result = new HashMap<>();
        for (Event e : events) {
            result.put(e.getId(), uriViews.getOrDefault("/events/" + e.getId(), 0L));
        }
        return result;
    }

    private <T> List<T> slice(List<T> list, int from, int size) {
        if (from >= list.size()) return List.of();
        int to = Math.min(list.size(), from + size);
        return list.subList(from, to);
    }

    private CategoryDto mapCategory(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        return dto;
    }

    private UserShortDto mapInitiator(User u) {
        UserShortDto dto = new UserShortDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        return dto;
    }
}
