package ru.practicum.ewm.requests.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.events.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/requests")
public class UserRequestController {

    private final RequestService requestService;


//Добавление запроса от текущего пользователя на участие в событии.
// /users/{userId}/requests?eventId={eventId}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable long userId,
                                                           @RequestParam @Positive long eventId) {
        return requestService.addParticipationRequest(userId, eventId);
    }


//Получение информации о заявках текущего пользователя на участие в чужих событиях.

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable long userId) {
        return requestService.getUserRequests(userId);
    }


//Отмена своего запроса на участие в событии.
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
