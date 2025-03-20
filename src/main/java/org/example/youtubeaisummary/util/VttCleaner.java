package org.example.youtubeaisummary.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VttCleaner {
    /**
     * VTT 자막 텍스트에서 헤더, 타임스탬프, 정렬 정보, HTML 태그 등을 제거합니다.
     */
    public static String clean(String rawSubtitles) {
        return Arrays.stream(rawSubtitles.split("\n"))
                .map(String::trim)
                // 헤더 및 메타데이터 제거
                .filter(line -> !(line.isEmpty()
                        || line.startsWith("WEBVTT")
                        || line.startsWith("Kind:")
                        || line.startsWith("Language:")))
                // 타임스탬프 및 "-->" 포함 라인 제거
                .filter(line -> !line.contains("-->"))
                // HTML 태그 제거 (<c> 등)
                .map(line -> line.replaceAll("<\\/?c>", ""))
                .map(line -> line.replaceAll("<[^>]+>", ""))
                // 여러 공백을 하나의 공백으로 변환
                .map(line -> line.replaceAll("\\s+", " "))
                .collect(Collectors.joining(" "))
                .trim();
    }
}