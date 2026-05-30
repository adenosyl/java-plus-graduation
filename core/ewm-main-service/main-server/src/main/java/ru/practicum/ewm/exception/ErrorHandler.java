package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ApiError build(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .status(status.toString())
                .reason(reason)
                .message(message)
                .errors(errors == null ? List.of() : errors)
                .timestamp(LocalDateTime.now().format(TS_FORMAT))
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "Для выполнения запрошенной операции условия не соблюдены", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, "Для выполнения запрошенной операции условия не соблюдены", ex.getMessage(), List.of());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manv) {
            List<String> errors = manv.getBindingResult().getFieldErrors().stream()
                    .map(this::formatFieldError)
                    .toList();
            return build(HttpStatus.BAD_REQUEST, "Получен некорректный запрос", "Ошибка валидации", errors);
        }
        return build(HttpStatus.BAD_REQUEST, "Получен некорректный запрос", ex.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDbConflict(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "Для выполнения запрошенной операции условия не соблюдены",
                "Нарушение целостности данных", List.of());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleOther(Throwable ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Непредвиденная ошибка", ex.getMessage(), List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, "Получен некорректный запрос", ex.getMessage(), List.of());
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}

