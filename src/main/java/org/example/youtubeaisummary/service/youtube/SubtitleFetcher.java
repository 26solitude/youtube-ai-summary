package org.example.youtubeaisummary.service.youtube;

import org.example.youtubeaisummary.dto.youtube.SubtitleDTO;
import org.example.youtubeaisummary.exception.youtube.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.youtube.YoutubeExtractionException;
import org.example.youtubeaisummary.util.youtube.ProcessExecutor;
import org.example.youtubeaisummary.util.youtube.SubtitleProcessor;
import org.example.youtubeaisummary.util.youtube.YoutubeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SubtitleFetcher {
    private static final Logger log = LoggerFactory.getLogger(SubtitleFetcher.class);
    private static final String TMP = "temp";

    public SubtitleDTO fetchSubtitle(String url) {
        String vid = extractVideoId(url);
        runYtDlp(vid, url);
        String subtitlePath = selectSubtitleFile(vid);
        String subtitleContent = readSubtitleFile(subtitlePath);

        String cleanedSubtitle = SubtitleProcessor.processSubtitleContent(subtitleContent);

        return new SubtitleDTO(cleanedSubtitle);
    }

    private String extractVideoId(String url) {
        try {
            return YoutubeUtil.extractVideoId(url);
        } catch (IllegalArgumentException e) {
            throw new YoutubeExtractionException(
                    YoutubeErrorCode.PARSING_ERROR,
                    url
            );
        }
    }

    private void runYtDlp(String vid, String url) {
        try {
            Path tmpDir = Path.of(TMP);
            if (Files.notExists(tmpDir)) Files.createDirectories(tmpDir);

            ProcessExecutor.ProcessResult r = ProcessExecutor.executeCommand(
                    "yt-dlp",
                    "--skip-download",
                    "--write-auto-sub",
                    "--sub-lang", "en",
                    "--sub-format", "vtt",
                    "--convert-subs", "vtt",
                    "-o", TMP + "/%(id)s.%(ext)s",
                    url
            );

            log.debug("yt-dlp exitCode={} output=\n{}", r.getExitCode(), r.getOutput());

            if (r.getExitCode() != 0) {
                throw new YoutubeExtractionException(
                        YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED,
                        r.getExitCode()
                );
            }
        } catch (Exception e) {
            log.error("yt-dlp 실행 오류", e);
            throw new YoutubeExtractionException(
                    YoutubeErrorCode.GENERAL_ERROR,
                    e.getMessage()
            );
        }
    }


    private String selectSubtitleFile(String vid) {
        Path en = Path.of(TMP, vid + ".en.vtt");

        if (Files.exists(en)) {
            return en.toString();
        } else {
            throw new YoutubeExtractionException(
                    YoutubeErrorCode.SUBTITLE_FILE_NOT_FOUND,
                    en.toString()
            );
        }
    }

    private String readSubtitleFile(String path) {
        try {
            return Files.readString(Path.of(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("자막 파일 읽기 오류: {}", path, e);
            throw new YoutubeExtractionException(
                    YoutubeErrorCode.FILE_READ_ERROR,
                    e.getMessage()
            );
        } finally {
            try {
                Files.deleteIfExists(Path.of(path));
            } catch (IOException e) {
                log.warn("Temp 자막 파일 삭제 실패: {}", path, e);
            }
        }
    }
}