package org.example.youtubeaisummary.dto.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int     status,
        int     code,
        String  error,    // HTTP 상태 텍스트
        String  message,  // userMessage
        String  path      // 요청 URI
) {}
