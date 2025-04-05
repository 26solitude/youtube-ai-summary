package org.example.youtubeaisummary.dto.youtube;

import lombok.Getter;

@Getter
public class VideoInfo {
    private final String title;
    private final String subtitles;

    public VideoInfo(String title, String subtitles) {
        this.title = title;
        this.subtitles = subtitles;
    }
}