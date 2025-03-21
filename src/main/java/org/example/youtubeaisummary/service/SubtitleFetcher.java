package org.example.youtubeaisummary.service;

import org.example.youtubeaisummary.exception.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.YoutubeExtractionException;
import org.example.youtubeaisummary.util.ProcessExecutor;
import org.example.youtubeaisummary.util.VttCleaner;
import org.example.youtubeaisummary.util.YoutubeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class SubtitleFetcher {
    private static final Logger logger = LoggerFactory.getLogger(SubtitleFetcher.class);
    private static final String TEMP_DIR = "temp";

    /**
     * 자막을 추출합니다.
     * 먼저 yt-dlp --list-subs를 이용하여 사용 가능한 자막 목록을 조회하고,
     * 우선순위(수동: ko → en, 자동: ko → en)에 따라 적합한 옵션을 선택한 후,
     * 해당 옵션으로 자막을 다운로드합니다.
     */
    public String fetchSubtitle(String videoUrl) {
        // 먼저 사용 가능한 자막 옵션을 확인합니다.
        String[] optionAndLang = determinePreferredSubtitleOption(videoUrl);
        if (optionAndLang == null) {
            throw new YoutubeExtractionException(YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED,
                    "No subtitles available for video: " + videoUrl);
        }
        // 선택한 옵션으로 자막을 다운로드합니다.
        String subtitles = fetchSubtitles(videoUrl, optionAndLang[0], optionAndLang[1]);
        if (subtitles != null && !subtitles.isEmpty()) {
            return subtitles;
        }
        throw new YoutubeExtractionException(YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED,
                "Failed to download subtitles using preferred option for video: " + videoUrl);
    }

    /**
     * yt-dlp --list-subs 명령으로 자막 목록을 조회한 후,
     * 우선순위에 맞게 옵션과 언어를 반환합니다.
     * 반환 배열: [0]=옵션("--write-sub" 또는 "--write-auto-sub"), [1]=언어("ko" 또는 "en")
     */
    private String[] determinePreferredSubtitleOption(String videoUrl) {
        try {
            // yt-dlp --list-subs <videoUrl>로 자막 목록 조회
            ProcessExecutor.ProcessResult result = ProcessExecutor.executeCommand("yt-dlp", "--list-subs", videoUrl);
            logger.info("Subtitles list for video: {}\n{}", videoUrl, result.getOutput());
            // 기본 우선순위 플래그
            boolean manualKo = result.getOutput().toLowerCase().contains("ko") &&
                    result.getOutput().toLowerCase().contains("manual");
            boolean manualEn = result.getOutput().toLowerCase().contains("en") &&
                    result.getOutput().toLowerCase().contains("manual");
            boolean autoKo = result.getOutput().toLowerCase().contains("ko") &&
                    result.getOutput().toLowerCase().contains("auto");
            boolean autoEn = result.getOutput().toLowerCase().contains("en") &&
                    result.getOutput().toLowerCase().contains("auto");

            // 우선순위: manual ko > manual en > auto ko > auto en
            if (manualKo) return new String[]{"--write-sub", "ko"};
            if (manualEn) return new String[]{"--write-sub", "en"};
            if (autoKo) return new String[]{"--write-auto-sub", "ko"};
            if (autoEn) return new String[]{"--write-auto-sub", "en"};
        } catch (Exception e) {
            logger.error("Error listing subtitles: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 주어진 옵션과 언어로 yt-dlp를 실행하여 자막 파일을 다운로드하고,
     * 파일 내용을 정제하여 반환합니다.
     */
    private String fetchSubtitles(String videoUrl, String subtitleOption, String lang) {
        String videoId = YoutubeUtil.extractVideoId(videoUrl);
        logger.info("Extracted videoId: {}", videoId);
        String outputTemplate = TEMP_DIR + File.separator + videoId;
        logger.info("Using output template: {} for language: {}", outputTemplate, lang);

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
            ProcessExecutor.ProcessResult result = ProcessExecutor.executeCommand(command);
            logger.info("YT-DLP Logs:\n{}", result.getOutput());
            logger.info("Exit Code: {}", result.getExitCode());
            if (result.getExitCode() != 0) {
                logger.warn("yt-dlp execution failed with exit code: {}", result.getExitCode());
                return null;
            }

            String filePath = outputTemplate + "." + lang + ".vtt";
            File subtitleFile = new File(filePath);
            if (subtitleFile.exists()) {
                String fileContent = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
                subtitleFile.delete(); // 파일 삭제
                return VttCleaner.clean(fileContent);
            }
            logger.warn("Subtitle file was not generated: {}", filePath);
            return null;
        } catch (Exception e) {
            logger.error("Error during yt-dlp execution: {}", e.getMessage());
            return null;
        }
    }
}