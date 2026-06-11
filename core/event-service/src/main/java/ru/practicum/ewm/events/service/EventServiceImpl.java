package ru.practicum.ewm.events.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.RequestFeignClient;
import ru.practicum.ewm.client.UserFeignClient;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.*;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.events.repository.EventSpecifications;
import ru.practicum.ewm.events.util.DateTimeUtil;
import ru.practicum.ewm.events.util.OffsetBasedPageRequest;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.stats.proto.recommendations.RecommendedEventProto;
import ru.practicum.stats.client.AnalyzerClient;


import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserFeignClient userFeignClient;
    private final RequestFeignClient requestFeignClient;
    private final AnalyzerClient analyzerClient;
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
                .and(EventSpecifications.eventDateBefore(end));

        if (sort == PublicEventSort.VIEWS) {
            List<Event> all = eventRepository.findAll(spec);

            if (Boolean.TRUE.equals(onlyAvailable)) {
                Map<Long, Long> confirmedMap = getConfirmedMap(all);

                all = all.stream()
                        .filter(event -> {
                            if (event.getParticipantLimit() == 0) {
                                return true;
                            }

                            long confirmed =
                                    confirmedMap.getOrDefault(event.getId(), 0L);

                            return confirmed < event.getParticipantLimit();
                        })
                        .toList();
            }

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

        if (Boolean.TRUE.equals(onlyAvailable)) {

            Map<Long, Long> confirmedMap = getConfirmedMap(page);

            page = page.stream()
                    .filter(event -> {
                        if (event.getParticipantLimit() == 0) {
                            return true;
                        }

                        long confirmed =
                                confirmedMap.getOrDefault(event.getId(), 0L);

                        return confirmed < event.getParticipantLimit();
                    })
                    .toList();
        }
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
                (root, query, cb) -> cb.equal(root.get("initiatorId"), userId),
                pageable
        ).getContent();

        return toShortDtosWithMeta(events);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto dto) {
                Category category = categoryRepository.findById(dto.getCategory())
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Категория с id=" + dto.getCategory() + " не найдена!"
                                ));

        // правило Swagger: не раньше чем через 2 часа
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("До даты события должно оставаться не менее 2 часов!");
        }

        Event e = new Event();
        e.setTitle(dto.getTitle());
        e.setAnnotation(dto.getAnnotation());
        e.setDescription(dto.getDescription());
        e.setCategory(category);
        e.setInitiatorId(userId);
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

        if (!Objects.equals(e.getInitiatorId(), userId)) {
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

        if (!Objects.equals(e.getInitiatorId(), userId)) {
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
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено!"));

        if (!Objects.equals(e.getInitiatorId(), userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено!");
        }

        return requestFeignClient.getEventRequests(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(
            long userId,
            long eventId,
            EventRequestStatusUpdateRequest dto) {

        ensureUserExists(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Событие с id=" + eventId + " не найдено!"
                        ));

        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new NotFoundException(
                    "Событие с id=" + eventId + " не найдено!"
            );
        }

        return requestFeignClient.changeRequestStatus(eventId, dto);
    }

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

    @Override
    @Transactional(readOnly = true)
    public EventDto getInternalEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Событие с id=" + eventId + " не найдено!")
                );

        return new EventDto(
                event.getId(),
                event.getInitiatorId(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                EventStateDto.valueOf(event.getState().name())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getRecommendations(
            Long userId,
            Integer maxResults
    ) {

        return analyzerClient
                .getRecommendations(
                        userId,
                        maxResults
                )
                .stream()
                .map(RecommendedEventProto::getEventId)
                .map(eventRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(event -> toShortDtosWithMeta(List.of(event)).getFirst())
                .toList();
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
            dto.setInitiator(mapInitiator(e.getInitiatorId()));

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
            dto.setInitiator(mapInitiator(e.getInitiatorId()));
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
        dto.setInitiator(mapInitiator(e.getInitiatorId()));
        dto.setConfirmedRequests(confirmed.getOrDefault(e.getId(), 0L));
        dto.setViews(views.getOrDefault(e.getId(), 0L));
        return dto;
    }

    private Map<Long, Long> getConfirmedMap(List<Event> events) {

        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        try {
            List<Long> eventIds = events.stream()
                    .map(Event::getId)
                    .toList();

            return requestFeignClient.getConfirmedCounts(eventIds);

        } catch (Exception e) {
            return Map.of();
        }
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
        return new CategoryDto(
            c.getId(),
            c.getName()
        );
    }

    private UserShortDto mapInitiator(Long userId) {

        try {
            ru.practicum.ewm.client.dto.UserDto user =
                    userFeignClient.getUser(userId);

            return new UserShortDto(
                user.id(),
                user.name()
            );
        } catch (Exception e) {

            return new UserShortDto(
                userId,
                "Unknown"
            );
        }
    }

    private void ensureUserExists(long userId) {
        try {
            userFeignClient.getUser(userId);
        } catch (NotFoundException e) {
            throw e;
        }
    }
}
