package ru.practicum.ewm.events.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.events.model.RequestUpdateStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotEmpty
    private List<Long> requestIds;

    @NotNull
    private RequestUpdateStatus status; // CONFIRMED / REJECTED
}
