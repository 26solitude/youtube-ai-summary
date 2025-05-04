package org.example.youtubeaisummary.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.youtubeaisummary.dto.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * 모든 컨트롤러에서 터진 예외를 잡아서
 * - BaseException (커스텀) 은 각 예외가 담고 있는 httpStatus/code/userMessage를 반환
 * - 그 외 Exception 은 500으로 통일
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * BaseException (YoutubeExtractionException 등) 전용 핸들러
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBase(BaseException ex,
                                                    HttpServletRequest req) {
        log.warn("Handled exception: {}", ex.getLogMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(makeErrorResponse(
                        ex.getHttpStatus().value(),
                        ex.getCode(),
                        ex.getHttpStatus().getReasonPhrase(),
                        ex.getUserMessage(),
                        req.getRequestURI()
                ));
    }

    /**
     * 그 외 예기치 못한 예외 핸들러
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex,
                                                     HttpServletRequest req) {
        log.error("Unhandled exception for {} {}", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(makeErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        5000,
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "내부 서버 오류가 발생했습니다.",
                        req.getRequestURI()
                ));
    }

    private ErrorResponse makeErrorResponse(int status,
                                            int code,
                                            String error,
                                            String message,
                                            String path) {
        return new ErrorResponse(
                Instant.now(),
                status,
                code,
                error,
                message,
                path
        );
    }
}