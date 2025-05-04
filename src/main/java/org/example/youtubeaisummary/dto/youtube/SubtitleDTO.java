package org.example.youtubeaisummary.dto.youtube;

import lombok.Getter;

@Getter
public class SubtitleDTO {
    private final String subtitles;

    public SubtitleDTO(String subtitles) {
        this.subtitles = subtitles;
    }
}