package org.example.youtubeaisummary.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(YoutubeExtractionException.class)
    public ResponseEntity<Map<String, Object>> handleYoutubeExtractionException(YoutubeExtractionException ex) {
        log.warn("YoutubeExtractionException 발생: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", ex.getCode());
        response.put("errorMessage", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred:", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", 5000);
        response.put("errorMessage", "내부 서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}