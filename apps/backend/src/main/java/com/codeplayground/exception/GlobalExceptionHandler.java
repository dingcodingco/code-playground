package com.codeplayground.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 글로벌 예외 처리기.
 * 애플리케이션에서 발생하는 모든 예외를 중앙에서 처리합니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 리소스를 찾을 수 없는 경우 처리합니다.
     *
     * @param ex 리소스 찾기 실패 예외
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            final ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());

        final Map<String, Object> errorResponse = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Not Found",
                "message", ex.getMessage(),
                "path", getCurrentPath()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 요청 데이터 유효성 검증 실패 시 처리합니다.
     *
     * @param ex 유효성 검증 실패 예외
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            final MethodArgumentNotValidException ex) {

        log.warn("Validation failed: {}", ex.getMessage());

        final Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        final Map<String, Object> errorResponse = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", "유효성 검증에 실패했습니다",
                "validationErrors", fieldErrors,
                "path", getCurrentPath()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 잘못된 인수 예외를 처리합니다.
     *
     * @param ex 잘못된 인수 예외
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            final IllegalArgumentException ex) {

        log.warn("Illegal argument: {}", ex.getMessage());

        final Map<String, Object> errorResponse = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", ex.getMessage(),
                "path", getCurrentPath()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 예상하지 못한 모든 예외를 처리합니다.
     *
     * @param ex 예외
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(final Exception ex) {
        log.error("Unexpected error occurred", ex);

        final Map<String, Object> errorResponse = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", "서버 내부 오류가 발생했습니다",
                "path", getCurrentPath()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 현재 요청 경로를 가져옵니다.
     * 실제 구현에서는 HttpServletRequest를 사용해야 하지만,
     * 여기서는 간단히 처리합니다.
     *
     * @return 요청 경로
     */
    private String getCurrentPath() {
        // TODO: HttpServletRequest를 통해 실제 경로 가져오기
        return "/api/v1/**";
    }
}