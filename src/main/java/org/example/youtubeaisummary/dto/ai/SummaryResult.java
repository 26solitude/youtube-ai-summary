package org.example.youtubeaisummary.dto.ai;

public class SummaryResult {
    private final String title;
    private final String summary;

    public SummaryResult(String title, String summary) {
        this.title = title;
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }
}