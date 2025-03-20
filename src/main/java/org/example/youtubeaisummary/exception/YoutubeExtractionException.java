package org.example.youtubeaisummary.exception;

public class YoutubeExtractionException extends BaseException {
    public YoutubeExtractionException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}