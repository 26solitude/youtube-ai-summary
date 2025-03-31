package org.example.youtubeaisummary.controller;

import org.example.youtubeaisummary.dto.youtube.VideoInfo;
import org.example.youtubeaisummary.service.youtube.YoutubeClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class YoutubeAISummaryController {
    private final YoutubeClient youtubeClient;

    public YoutubeAISummaryController(YoutubeClient youtubeClient) {
        this.youtubeClient = youtubeClient;
    }

    @GetMapping("/api/video-info")
    public Map<String, String> getVideoInfo(@RequestParam String url) {
        VideoInfo info = youtubeClient.fetchVideoInfo(url);
        // LinkedHashMap를 사용하여 필드 순서를 보장합니다.
        Map<String, String> result = new LinkedHashMap<>();
        result.put("title", info.getTitle());
        result.put("subtitles", info.getSubtitles());
        return result;
    }
}