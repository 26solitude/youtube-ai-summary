package org.example.youtubeaisummary.dto.ai;

import lombok.Getter;

@Getter
public class SummaryResult {
    private final String summary;

    public SummaryResult(String summary) {
        this.summary = summary;
    }
}