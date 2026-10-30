package auth.service.exception.handler;

import auth.service.exception.BadCredentialsException;
import auth.service.exception.EmailAlreadyExistsException;
import auth.service.exception.NotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmailExists(EmailAlreadyExistsException ex) {
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Конфликт данных")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleBadCredentials(BadCredentialsException ex) {
        return ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .reason("Ошибка авторизации")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidArguments(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        String message = String.join("\n", errors);

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Ошибка валидации")
                .message(message)
                .errors(errors)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable root = ex.getMostSpecificCause();
        String message = root.getMessage();

        if (root instanceof InvalidFormatException ife) {
            message = "Invalid value: " + ife.getValue();
        }

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Ошибка валидации")
                .message(message)
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintConflict(DataIntegrityViolationException ex) {
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Конфликт ограничений")
                .message("Data integrity violation")
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

    private String formatFieldError(FieldError fe) {
        String field = fe.getField();
        String msg = fe.getDefaultMessage();
        return field + ": " + msg;
    }
}