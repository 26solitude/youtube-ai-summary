package org.example.youtubeaisummary.service;

import org.example.youtubeaisummary.dto.VideoInfo;
import org.springframework.stereotype.Service;

@Service
public class YoutubeClient {
    private final TitleFetcher titleFetcher;
    private final SubtitleFetcher subtitleFetcher;

    public YoutubeClient(TitleFetcher titleFetcher, SubtitleFetcher subtitleFetcher) {
        this.titleFetcher = titleFetcher;
        this.subtitleFetcher = subtitleFetcher;
    }

    public VideoInfo fetchVideoInfo(String videoUrl) {
        String title = titleFetcher.fetchTitle(videoUrl);
        String subtitles = subtitleFetcher.fetchSubtitle(videoUrl);
        return new VideoInfo(title, subtitles);
    }
}