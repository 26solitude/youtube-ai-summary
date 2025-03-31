package org.example.youtubeaisummary.dto.youtube;

public class VideoInfo {
    private final String title;
    private final String subtitles;

    public VideoInfo(String title, String subtitles) {
        this.title = title;
        this.subtitles = subtitles;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitles() {
        return subtitles;
    }
}