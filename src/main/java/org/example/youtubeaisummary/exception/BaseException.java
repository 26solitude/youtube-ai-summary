package org.example.youtubeaisummary.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    public BaseException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.httpStatus = errorCode.getHttpStatus();
        this.code = errorCode.getCode();
        this.message = String.format(errorCode.getMessage(), args);
    }
}