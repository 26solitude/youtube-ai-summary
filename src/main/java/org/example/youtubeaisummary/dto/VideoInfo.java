package org.example.youtubeaisummary.dto;

public class VideoInfo {
    private String title;
    private String subtitles;

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