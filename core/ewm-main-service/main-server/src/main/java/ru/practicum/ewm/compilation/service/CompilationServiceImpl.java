package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.events.dto.CategoryDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.UserShortDto;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.RequestStatus;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.events.repository.ParticipationRequestRepository;
import ru.practicum.ewm.events.service.StatsFacade;
import ru.practicum.ewm.events.util.OffsetBasedPageRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.users.model.User;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsFacade statsFacade;

    @Override
    public CompilationDto create(NewCompilationDto dto) {
        Compilation c = new Compilation();
        c.setTitle(dto.getTitle());
        c.setPinned(dto.getPinned() != null ? dto.getPinned() : Boolean.FALSE);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            c.setEvents(loadEvents(dto.getEvents()));
        }

        Compilation saved = compilationRepository.save(c);
        return toDtoWithEvents(saved);
    }

    @Override
    public void delete(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id= " + compId + " не найдена");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto update(long compId, UpdateCompilationRequest dto) {
        Compilation c = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id =" + compId + " не найдена"));

        if (dto.getTitle() != null) {
            if (dto.getTitle().isBlank()) {
                throw new IllegalArgumentException("Заголовок не должен быть пустым");
            }
            c.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            c.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            // если прислали пустой список — очищаем
            c.setEvents(loadEvents(dto.getEvents()));
        }

        Compilation saved = compilationRepository.save(c);
        return toDtoWithEvents(saved);
    }

    @Override
    public List<CompilationDto> getPublicCompilations(Boolean pinned, int from, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(from, size, sort);

        Page<Compilation> page = (pinned == null)
                ? compilationRepository.findAll(pageable)
                : compilationRepository.findAllByPinned(pinned, pageable);

        return page.getContent().stream()
                .map(this::toDtoWithEvents)
                .toList();
    }

    @Override
    public CompilationDto getPublicCompilation(long compId) {
        Compilation c = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id= " + compId + " не найдена"));
        return toDtoWithEvents(c);
    }

    private Set<Event> loadEvents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();

        List<Event> found = eventRepository.findAllById(ids);
        if (found.size() != new HashSet<>(ids).size()) {
            Set<Long> existing = new HashSet<>(found.stream().map(Event::getId).toList());
            Long missing = ids.stream().filter(id -> !existing.contains(id)).findFirst().orElse(null);
            throw new NotFoundException("Событие с id=" + missing + " не найдено");
        }

        found.sort(Comparator.comparingLong(Event::getId));
        return new LinkedHashSet<>(found);
    }

    private CompilationDto toDtoWithEvents(Compilation c) {
        List<Event> events = new ArrayList<>(c.getEvents());
        List<EventShortDto> eventDtos = toShortDtosWithMeta(events);
        return CompilationMapper.toDto(c, eventDtos);
    }

    private List<EventShortDto> toShortDtosWithMeta(List<Event> events) {
        if (events == null || events.isEmpty()) return List.of();

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
