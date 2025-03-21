package org.example.youtubeaisummary.service;

import org.example.youtubeaisummary.exception.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.YoutubeExtractionException;
import org.example.youtubeaisummary.util.VttCleaner;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SubtitleFetcher {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SubtitleFetcher.class);
    private static final String TEMP_DIR = "temp";

    /**
     * 자막을 추출합니다.
     * 우선 manual 자막을 한국어("ko")로, 없으면 영어("en") manual 자막을,
     * 그래도 없으면 자동 자막을 한국어("ko")로, 없으면 영어("en") 자동 자막을 시도합니다.
     */
    public String fetchSubtitle(String videoUrl) {
        // 1. manual 자막: 한국어 시도
        String subtitles = fetchSubtitles(videoUrl, "--write-sub", "ko");
        if (subtitles != null && !subtitles.isEmpty()) {
            return subtitles;
        }
        // 2. manual 자막: 영어 시도
        subtitles = fetchSubtitles(videoUrl, "--write-sub", "en");
        if (subtitles != null && !subtitles.isEmpty()) {
            return subtitles;
        }
        // 3. 자동 자막: 한국어 시도
        subtitles = fetchSubtitles(videoUrl, "--write-auto-sub", "ko");
        if (subtitles != null && !subtitles.isEmpty()) {
            return subtitles;
        }
        // 4. 자동 자막: 영어 시도
        subtitles = fetchSubtitles(videoUrl, "--write-auto-sub", "en");
        if (subtitles != null && !subtitles.isEmpty()) {
            return subtitles;
        }
        // 모든 시도가 실패하면 최종적으로 예외 발생
        throw new YoutubeExtractionException(YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED, "No subtitles available for video: " + videoUrl);
    }

    /**
     * 주어진 옵션과 언어로 yt-dlp를 실행하여 자막 파일을 다운로드하고,
     * 파일 내용을 정제하여 반환합니다.
     */
    private String fetchSubtitles(String videoUrl, String subtitleOption, String lang) {
        String videoId = extractVideoId(videoUrl);
        logger.info("Extracted videoId: " + videoId);
        String outputTemplate = TEMP_DIR + File.separator + videoId;
        logger.info("Using output template: " + outputTemplate + " for language: " + lang);

        String[] command = {
                "yt-dlp",
                subtitleOption,
                "--sub-lang", lang,
                "--skip-download",
                "--sub-format", "vtt",
                "-o", outputTemplate,
                videoUrl
        };

        try {
            // TEMP_DIR 생성
            File tempDir = new File(TEMP_DIR);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String logs = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            logger.info("YT-DLP Logs:\n" + logs);

            int exitCode = process.waitFor();
            logger.info("Exit Code: " + exitCode);
            if (exitCode != 0) {
                logger.warn("yt-dlp 실행 실패, 종료 코드: " + exitCode);
                return null;  // 실패 시 null 반환하여 다음 옵션 시도
            }

            String filePath = outputTemplate + "." + lang + ".vtt";
            File subtitleFile = new File(filePath);
            if (subtitleFile.exists()) {
                String fileContent = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
                subtitleFile.delete(); // 파일 삭제
                return VttCleaner.clean(fileContent);
            }
            logger.warn("자막 파일이 생성되지 않았습니다: " + filePath);
            return null;
        } catch (Exception e) {
            logger.error("yt-dlp 실행 중 오류 발생: " + e.getMessage());
            return null;
        }
    }

    private String extractVideoId(String url) {
        Pattern pattern = Pattern.compile("v=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("유효하지 않은 YouTube URL: " + url);
        }
    }
}
