package org.example.youtubeaisummary.dto.ai;

import lombok.Getter;

@Getter
public class SummaryResult {
    private final String title;
    private final String summary;

    public SummaryResult(String title, String summary) {
        this.title = title;
        this.summary = summary;
    }
}