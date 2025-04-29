package org.example.youtubeaisummary.exception.youtube;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.youtubeaisummary.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum YoutubeErrorCode implements ErrorCode {
    TITLE_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 1001, "동영상 제목 추출에 실패했습니다. 종료 코드: %d"),
    SUBTITLE_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 1002, "자막 추출에 실패했습니다. 종료 코드: %s"),
    SUBTITLE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, 1003, "자막 파일이 생성되지 않았습니다: %s"),
    GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1004, "알 수 없는 내부 오류가 발생했습니다: %s"),
    PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1005, "메타데이터 파싱 중 오류가 발생했습니다: %s"),
    FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1006, "파일 읽기 중 오류가 발생했습니다: %s");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}