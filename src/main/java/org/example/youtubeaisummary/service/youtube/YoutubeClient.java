package org.example.youtubeaisummary.service.youtube;

import org.example.youtubeaisummary.dto.youtube.VideoInfo;
import org.springframework.stereotype.Service;

@Service
public class YoutubeClient {
    private final MetadataFetcher metadataFetcher;
    private final SubtitleFetcher subtitleFetcher;

    public YoutubeClient(MetadataFetcher m, SubtitleFetcher s) {
        this.metadataFetcher = m; this.subtitleFetcher = s;
    }

    public VideoInfo fetchVideoInfo(String url) {
        MetadataFetcher.VideoMeta meta = metadataFetcher.fetch(url);
        String subs = subtitleFetcher.fetchSubtitle(meta, url);
        return new VideoInfo(meta.title(), subs);
    }
}