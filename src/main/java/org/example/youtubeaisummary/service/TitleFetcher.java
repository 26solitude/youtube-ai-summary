package org.example.youtubeaisummary.service;

import org.example.youtubeaisummary.exception.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.YoutubeExtractionException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class TitleFetcher {
    private static final Logger logger = Logger.getLogger(TitleFetcher.class.getName());

    public String fetchTitle(String videoUrl) {
        String[] command = {"yt-dlp", "--get-title", videoUrl};
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // 인코딩은 필요에 따라 "MS949" 또는 "Cp949" 사용 (여기서는 MS949 예시)
            String title = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("MS949")))
                    .lines().collect(Collectors.joining("\n")).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new YoutubeExtractionException(YoutubeErrorCode.TITLE_EXTRACTION_FAILED, exitCode);
            }
            return title;
        } catch (Exception e) {
            throw new YoutubeExtractionException(YoutubeErrorCode.GENERAL_ERROR, e.getMessage());
        }
    }
}