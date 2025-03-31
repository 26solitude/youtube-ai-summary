package org.example.youtubeaisummary.exception.youtube;

import org.example.youtubeaisummary.exception.BaseException;
import org.example.youtubeaisummary.exception.ErrorCode;

public class YoutubeExtractionException extends BaseException {
    public YoutubeExtractionException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}