package org.example.youtubeaisummary.service.youtube;

import org.example.youtubeaisummary.exception.youtube.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.youtube.YoutubeExtractionException;
import org.example.youtubeaisummary.util.youtube.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class TitleFetcher {
    private static final Logger logger = LoggerFactory.getLogger(TitleFetcher.class);

    public String fetchTitle(String videoUrl) {
        String[] command = {"yt-dlp", "--get-title", videoUrl};
        try {
            ProcessExecutor.ProcessResult result = ProcessExecutor.executeCommand(command);
            if (result.getExitCode() != 0 || result.getOutput().isEmpty()) {
                throw new YoutubeExtractionException(YoutubeErrorCode.TITLE_EXTRACTION_FAILED, result.getExitCode());
            }
            String title = result.getOutput().trim();
            logger.info("Fetched title: {}", title);
            return title;
        } catch (Exception e) {
            throw new YoutubeExtractionException(YoutubeErrorCode.GENERAL_ERROR, e.getMessage());
        }
    }
}