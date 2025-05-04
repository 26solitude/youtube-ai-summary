package org.example.youtubeaisummary.exception.youtube;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.youtubeaisummary.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum YoutubeErrorCode implements ErrorCode {
    /** yt-dlp가 제목 추출 단계에서 비정상 종료된 경우 */
    TITLE_EXTRACTION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR, 1001,
            "동영상 제목을 읽어올 수 없습니다.",
            "yt-dlp 비정상 종료 (exit=%d)"
    ),

    /** yt-dlp가 자막 추출 단계에서 비정상 종료된 경우 */
    SUBTITLE_EXTRACTION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR, 1002,
            "자막을 추출할 수 없습니다.",
            "yt-dlp 비정상 종료 (exit=%d)"
    ),

    /** 다운로드된 자막 파일(.vtt)이 존재하지 않을 때 */
    SUBTITLE_FILE_NOT_FOUND(
            HttpStatus.NOT_FOUND, 1003,
            "자막 파일이 생성되지 않았습니다.",
            "자막 파일 없음: %s 또는 %s"
    ),

    /** 메타데이터(제목·ID) 파싱 중 예외가 발생했을 때 */
    PARSING_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR, 1004,
            "메타데이터를 파싱할 수 없습니다.",
            "파싱 오류: %s"
    ),

    /** 로컬 파일 읽기에 실패했을 때 */
    FILE_READ_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR, 1005,
            "파일을 읽을 수 없습니다.",
            "파일 읽기 오류: %s"
    ),

    /** 그 외 예기치 못한 내부 오류 */
    GENERAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR, 1006,
            "내부 서버 오류가 발생했습니다.",
            "예기치 못한 오류: %s"
    );

    private final HttpStatus httpStatus;
    private final int        code;
    private final String     userMessage;
    private final String     logTemplate;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public int getCode() {
        return code;
    }

    /** 클라이언트에게 보여줄 메시지 */
    @Override
    public String getMessage() {
        return userMessage;
    }

    /** 로그에 사용할 포맷 문자열 */
    public String getLogTemplate() {
        return logTemplate;
    }
}
