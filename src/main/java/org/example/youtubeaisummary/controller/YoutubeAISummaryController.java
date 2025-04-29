package org.example.youtubeaisummary.controller;

import org.example.youtubeaisummary.dto.ai.SummaryResult;
import org.example.youtubeaisummary.dto.youtube.VideoInfo;
import org.example.youtubeaisummary.service.ai.AISummaryService;
import org.example.youtubeaisummary.service.youtube.YoutubeClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class YoutubeAISummaryController {
    private final YoutubeClient youtubeClient;
    private final AISummaryService summaryService;

    public YoutubeAISummaryController(YoutubeClient youtubeClient, AISummaryService summaryService) {
        this.youtubeClient = youtubeClient;
        this.summaryService = summaryService;
    }

    @GetMapping("/api/video-info")
    public SummaryResult getVideoInfo(@RequestParam String url) throws Exception {
        VideoInfo info = youtubeClient.fetchVideoInfo(url);

        SummaryResult summary = summaryService.generateSummary(info);

        return summary;
    }
}