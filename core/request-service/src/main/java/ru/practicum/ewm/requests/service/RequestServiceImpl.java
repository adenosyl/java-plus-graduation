package ru.practicum.ewm.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.EventFeignClient;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.RequestUpdateStatus;
import ru.practicum.ewm.requests.mapper.RequestMapper;
import ru.practicum.ewm.requests.model.ParticipationRequest;
import ru.practicum.ewm.requests.model.RequestStatus;
import ru.practicum.ewm.requests.repository.EventConfirmedCount;
import ru.practicum.ewm.requests.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final EventFeignClient eventFeignClient;
    private final ParticipationRequestRepository requestRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(long userId, long eventId) {

        EventDto event = eventFeignClient.getEvent(eventId);

        if (Objects.equals(event.initiatorId(), userId)) {
            throw new ConflictException("Инициатор мероприятия не может выступать в роли участника!");
        }

        if (event.state() != EventStateDto.PUBLISHED) {
            throw new ConflictException("Подача запросов допускается только для опубликованных событий.");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Запрос на участие уже был отправлен.");
        }

        if (event.participantLimit() > 0) {
            long confirmed = requestRepository.countByEventIdAndStatus(
                    eventId,
                    RequestStatus.CONFIRMED
            );

            if (confirmed >= event.participantLimit()) {
                throw new ConflictException("Превышен лимит по числу участников!");
            }
        }

        ParticipationRequest pr = new ParticipationRequest();
        pr.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        pr.setEventId(eventId);
        pr.setRequesterId(userId);

        if (event.participantLimit() == 0
                || !Boolean.TRUE.equals(event.requestModeration())) {
            pr.setStatus(RequestStatus.CONFIRMED);
        } else {
            pr.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest saved = requestRepository.save(pr);

        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        return requestRepository.findAllByRequesterIdOrderByIdAsc(userId)
                .stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {

        ParticipationRequest pr = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Запрос с id=" + requestId + " не найден!")
                );

        pr.setStatus(RequestStatus.CANCELED);

        ParticipationRequest saved = requestRepository.save(pr);

        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long eventId) {

        return requestRepository.findAllByEventIdOrderByIdAsc(eventId)
                .stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(
            Long eventId,
            EventRequestStatusUpdateRequest request) {

        List<ParticipationRequest> requests =
                requestRepository.findAllByIdIn(request.getRequestIds());

        EventRequestStatusUpdateResult result =
                new EventRequestStatusUpdateResult();

        for (ParticipationRequest participationRequest : requests) {

            if (request.getStatus() == RequestUpdateStatus.CONFIRMED) {

                participationRequest.setStatus(RequestStatus.CONFIRMED);

                result.getConfirmedRequests()
                        .add(RequestMapper.toDto(participationRequest));

            } else {

                participationRequest.setStatus(RequestStatus.REJECTED);

                result.getRejectedRequests()
                        .add(RequestMapper.toDto(participationRequest));
            }
        }

        requestRepository.saveAll(requests);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {

        return requestRepository
                .countByEventIdsAndStatus(
                        eventIds,
                        RequestStatus.CONFIRMED
                )
                .stream()
                .collect(Collectors.toMap(
                        EventConfirmedCount::getEventId,
                        EventConfirmedCount::getCnt
                ));
    }
}