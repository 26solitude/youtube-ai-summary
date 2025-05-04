package org.example.youtubeaisummary.util.youtube;

import org.example.youtubeaisummary.exception.youtube.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.youtube.YoutubeExtractionException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUtil {
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("v=([^&]+)");

    public static String extractVideoId(String url) {
        // 1) null·빈 문자열 체크
        if (url == null || url.isBlank()) {
            throw new YoutubeExtractionException(
                    YoutubeErrorCode.PARSING_ERROR,
                    "URL이 비어 있습니다."
            );
        }

        // 2) 정규식으로 v= 뒤 ID 추출
        Matcher matcher = VIDEO_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 3) 매칭 실패 시 파싱 에러 코드로 예외
        throw new YoutubeExtractionException(
                YoutubeErrorCode.PARSING_ERROR,
                url  // 템플릿 "파싱 오류: %s" 에 이 URL이 들어갑니다
        );
    }
}