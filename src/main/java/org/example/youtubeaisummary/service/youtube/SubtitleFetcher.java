package org.example.youtubeaisummary.service.youtube;

import org.example.youtubeaisummary.exception.youtube.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.youtube.YoutubeExtractionException;
import org.example.youtubeaisummary.util.youtube.ProcessExecutor;
import org.example.youtubeaisummary.util.youtube.VttCleaner;
import org.example.youtubeaisummary.util.youtube.YoutubeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@Component
public class SubtitleFetcher {
    private static final Logger logger = LoggerFactory.getLogger(SubtitleFetcher.class);
    private static final String TEMP_DIR = "temp";

    private final SentenceSplitterService sentenceSplitterService;
    private final DuplicateRemovalService duplicateRemovalService;

    public SubtitleFetcher(SentenceSplitterService sentenceSplitterService,
                           DuplicateRemovalService duplicateRemovalService) {
        this.sentenceSplitterService = sentenceSplitterService;
        this.duplicateRemovalService = duplicateRemovalService;
    }

    public String fetchSubtitle(String videoUrl) {
        // 1) 자막 옵션 결정
        String[] optionAndLang = determinePreferredSubtitleOption(videoUrl);
        if (optionAndLang == null) {
            throw new YoutubeExtractionException(YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED,
                    "No subtitles for video: " + videoUrl);
        }
        // 2) 자막 다운로드 + 처리
        String subtitles = fetchSubtitles(videoUrl, optionAndLang[0], optionAndLang[1]);
        if (subtitles != null && !subtitles.isEmpty()) {
            return subtitles;
        }
        throw new YoutubeExtractionException(YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED,
                "Failed to download subtitles for video: " + videoUrl);
    }

    private String[] determinePreferredSubtitleOption(String videoUrl) {
        try {
            ProcessExecutor.ProcessResult result = ProcessExecutor.executeCommand("yt-dlp", "--list-subs", videoUrl);
            logger.info("Subtitles list for {}:\n{}", videoUrl, result.getOutput());

            boolean manualKo = result.getOutput().toLowerCase().contains("ko") &&
                    result.getOutput().toLowerCase().contains("manual");
            boolean manualEn = result.getOutput().toLowerCase().contains("en") &&
                    result.getOutput().toLowerCase().contains("manual");
            boolean autoKo = result.getOutput().toLowerCase().contains("ko") &&
                    result.getOutput().toLowerCase().contains("auto");
            boolean autoEn = result.getOutput().toLowerCase().contains("en") &&
                    result.getOutput().toLowerCase().contains("auto");

            if (manualKo) return new String[]{"--write-sub", "ko"};
            if (manualEn) return new String[]{"--write-sub", "en"};
            if (autoKo) return new String[]{"--write-auto-sub", "ko"};
            if (autoEn) return new String[]{"--write-auto-sub", "en"};
        } catch (Exception e) {
            logger.error("Error listing subtitles: {}", e.getMessage());
        }
        return null;
    }

    private String fetchSubtitles(String videoUrl, String subtitleOption, String lang) {
        String videoId = YoutubeUtil.extractVideoId(videoUrl);
        logger.info("VideoID: {}", videoId);

        String outputTemplate = TEMP_DIR + File.separator + videoId;
        logger.info("Output template: {}, lang: {}", outputTemplate, lang);

        String[] command = {
                "yt-dlp", subtitleOption,
                "--sub-lang", lang,
                "--skip-download",
                "--sub-format", "vtt",
                "-o", outputTemplate,
                videoUrl
        };

        try {
            File tempDir = new File(TEMP_DIR);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            ProcessExecutor.ProcessResult result = ProcessExecutor.executeCommand(command);
            logger.info("YT-DLP:\n{}", result.getOutput());
            if (result.getExitCode() != 0) {
                logger.warn("yt-dlp failed with exit code {}", result.getExitCode());
                return null;
            }

            String filePath = outputTemplate + "." + lang + ".vtt";
            File subtitleFile = new File(filePath);

            if (subtitleFile.exists()) {
                String fileContent = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
                subtitleFile.delete(); // 삭제 (임시 파일)

                // 1) VTT Cleaner
                String cleanedText = VttCleaner.clean(fileContent);

                // 2) [A] KSS 전, 긴 문자열에서 "연속 구절" 제거
                String noRepeatText = duplicateRemovalService.removeAllRepeatedSequencesGlobal(cleanedText);

                // 3) [B] 문장 분리 (KSS)
                ArrayList<String> sentences = sentenceSplitterService.splitSentences(noRepeatText);

                // 4) [C] 문장 간 중복 제거
                ArrayList<String> uniqueSentences = duplicateRemovalService.removeDuplicates(sentences);

                // 5) 최종 문자열로 합쳐 반환
                return String.join(". ", uniqueSentences);
            } else {
                logger.warn("Subtitle file not found: {}", filePath);
            }
        } catch (Exception e) {
            logger.error("Error fetching subtitles: {}", e.getMessage());
        }

        return null;
    }
}
