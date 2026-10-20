package catalog.service.exception.handler;

import catalog.service.exception.ConflictException;
import catalog.service.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex) {
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("Запрашиваемый ресурс не найден")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidArguments(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Ошибка валидации")
                .message(errorMessage)
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleBusinessConflict(ConflictException ex) {
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Конфликт данных")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintConflict(DataIntegrityViolationException ex) {
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Конфликт ограничений")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericError(Exception ex) {
        log.error("Internal server error", ex);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Ошибка сервера")
                .message(ex.getMessage())
                .build();
    }
}
