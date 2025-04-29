package org.example.youtubeaisummary.service.youtube;

import org.example.youtubeaisummary.exception.youtube.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.youtube.YoutubeExtractionException;
import org.example.youtubeaisummary.util.youtube.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MetadataFetcher {
    private static final Logger log = LoggerFactory.getLogger(MetadataFetcher.class);

    public VideoMeta fetch(String url) {
        try {
            String format = "%(title)s|%(automatic_captions)s|%(subtitles)s";
            ProcessExecutor.ProcessResult r = ProcessExecutor.executeCommand("yt-dlp", "--print", format, url);
            if (r.getExitCode() != 0)
                throw new YoutubeExtractionException(YoutubeErrorCode.GENERAL_ERROR, r.getOutput());

            String[] parts = r.getOutput().split("\\|", 3);
            if (parts.length < 3)
                throw new YoutubeExtractionException(YoutubeErrorCode.GENERAL_ERROR, "unexpected yt‑dlp output");

            String title = parts[0].trim();
            Map<String, Boolean> subs = parseSubs(parts[1], parts[2]);
            log.info("[META] title='{}' subtitles={}", title, subs);
            return new VideoMeta(title, subs);
        } catch (Exception e){
            throw new YoutubeExtractionException(YoutubeErrorCode.TITLE_EXTRACTION_FAILED,
                    "yt-dlp 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Map<String, Boolean> parseSubs(String autoJson, String manualJson) {
        Map<String, Boolean> map = new HashMap<>();
        autoJson = autoJson.replaceAll("[{}'\s]", "");
        manualJson = manualJson.replaceAll("[{}'\s]", "");
        for (String s : autoJson.split(",")) if (!s.isBlank()) map.put(s.split(":")[0], false);
        for (String s : manualJson.split(",")) if (!s.isBlank()) map.put(s.split(":")[0], true);
        return map;
    }

    public record VideoMeta(String title, Map<String, Boolean> subtitles) {
    }
}