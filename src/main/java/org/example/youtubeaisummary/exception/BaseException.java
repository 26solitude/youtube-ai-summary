package org.example.youtubeaisummary.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 커스텀 예외의 베이스가 되는 추상 클래스.
 * 클라이언트에게 보여줄 메시지(userMessage)와
 * 로그에 남길 메시지(logMessage)를 분리합니다.
 */
public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final int code;
    private final String userMessage;
    private final String logMessage;

    /**
     * @param httpStatus  HTTP 응답 코드
     * @param code        내부 에러 코드
     * @param userMessage 클라이언트에게 보여줄 메시지
     * @param logMessage  로그에 남길 상세 메시지
     */
    protected BaseException(HttpStatus httpStatus,
                            int code,
                            String userMessage,
                            String logMessage) {
        // super에는 로그용 메시지를 넣어야 스택트레이스에도 찍힙니다.
        super(logMessage);
        this.httpStatus = httpStatus;
        this.code = code;
        this.userMessage = userMessage;
        this.logMessage = logMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getLogMessage() {
        return logMessage;
    }
}