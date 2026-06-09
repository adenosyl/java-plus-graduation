package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.model.RequestUpdateStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotEmpty
    private List<Long> requestIds;

    @NotNull
    private RequestUpdateStatus status; // CONFIRMED / REJECTED
}
