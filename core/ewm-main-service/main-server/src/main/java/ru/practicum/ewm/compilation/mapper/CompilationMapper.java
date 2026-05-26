package ru.practicum.ewm.compilation.mapper;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.events.dto.EventShortDto;

import java.util.List;

public final class CompilationMapper {

    private CompilationMapper() {
    }

    public static CompilationDto toDto(Compilation c, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .pinned(c.getPinned())
                .events(events)
                .build();
    }
}
