package org.example.youtubeaisummary.service.youtube;

import org.example.youtubeaisummary.dto.youtube.SubtitleDTO;
import org.springframework.stereotype.Service;

@Service
public class YoutubeClient {
    private final SubtitleFetcher subtitleFetcher;

    public YoutubeClient(SubtitleFetcher subtitleFetcher) {
        this.subtitleFetcher = subtitleFetcher;
    }

    public SubtitleDTO fetchVideoInfo(String url) {
        SubtitleDTO subtitleDTO = subtitleFetcher.fetchSubtitle(url);
        return subtitleDTO;
    }
}