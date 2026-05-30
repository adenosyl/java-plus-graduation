package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    /**
     * Создает новую подборку.
     *
     * @param dto данные для создания подборки
     * @return созданная подборка
     */
    CompilationDto create(NewCompilationDto dto);

    /**
     * Удаляет подборку по идентификатору.
     *
     * @param compId идентификатор подборки
     */
    void delete(long compId);

    /**
     * Обновляет существующую подборку по идентификатору.
     *
     * @param compId идентификатор подборки
     * @param dto    данные для обновления подборки
     * @return обновленная подборка
     */
    CompilationDto update(long compId, UpdateCompilationRequest dto);

    /**
     * Возвращает список подборок для публичной части приложения.
     * Поддерживает фильтрацию по признаку закрепления (pinned) и пагинацию.
     *
     * @param pinned фильтр закрепленных подборок; если {@code null} — возвращаются все подборки
     * @param from   смещение (offset) от начала списка, неотрицательное значение
     * @param size   размер страницы (page size), положительное значение
     * @return список подборок
     */
    List<CompilationDto> getPublicCompilations(Boolean pinned, int from, int size);

    /**
     * Возвращает подборку по идентификатору для публичной части приложения.
     *
     * @param compId идентификатор подборки
     * @return подборка
     */
    CompilationDto getPublicCompilation(long compId);
}
