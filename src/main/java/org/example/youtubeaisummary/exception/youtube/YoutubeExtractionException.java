package org.example.youtubeaisummary.exception.youtube;

import org.example.youtubeaisummary.exception.BaseException;

/**
 * Youtube 관련 예외를 모두 이 클래스로 감쌉니다.
 * ErrorCode enum에서 정의된 httpStatus, code, userMessage, logTemplate 을 받아 처리합니다.
 */
public class YoutubeExtractionException extends BaseException {
    public YoutubeExtractionException(YoutubeErrorCode errorCode, Object... args) {
        super(
                errorCode.getHttpStatus(),
                errorCode.getCode(),
                /* 클라이언트용 메시지 */   errorCode.getUserMessage(),
                /* 로그용 메시지 */         String.format(errorCode.getLogTemplate(), args)
        );
    }
}