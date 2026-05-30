package ru.practicum.ewm.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.events.mapper.RequestMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.EventState;
import ru.practicum.ewm.events.model.ParticipationRequest;
import ru.practicum.ewm.events.model.RequestStatus;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.events.repository.ParticipationRequestRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    @Transactional
    public ru.practicum.ewm.events.dto.ParticipationRequestDto addParticipationRequest(long userId, long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден!"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено!"));

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Инициатор мероприятия не может выступать в роли участника!");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Подача запросов допускается только для опубликованных событий.");
        }

        if (requestRepository.existsByEvent_IdAndRequester_Id(eventId, userId)) {
            throw new ConflictException("Запрос на участие уже был отправлен.");
        }

        if (event.getParticipantLimit() > 0) {
            long confirmed = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("Превышен лимит по числу участников!");
            }
        }

        ParticipationRequest pr = new ParticipationRequest();
        pr.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        pr.setEvent(event);
        pr.setRequester(requester);

        // Если лимит = 0 или модерация выключена — запрос сразу CONFIRMED, иначе PENDING
        if (event.getParticipantLimit() == 0 || !Boolean.TRUE.equals(event.getRequestModeration())) {
            pr.setStatus(RequestStatus.CONFIRMED);
        } else {
            pr.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest saved = requestRepository.save(pr);
        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ru.practicum.ewm.events.dto.ParticipationRequestDto> getUserRequests(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден!");
        }

        return requestRepository.findAllByRequester_IdOrderByIdAsc(userId)
                .stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ru.practicum.ewm.events.dto.ParticipationRequestDto cancelRequest(long userId, long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден!");
        }

        ParticipationRequest pr = requestRepository.findByIdAndRequester_Id(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден!"));

        pr.setStatus(RequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(pr);

        return RequestMapper.toDto(saved);
    }
}
