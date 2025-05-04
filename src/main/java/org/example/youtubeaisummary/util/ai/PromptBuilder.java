package org.example.youtubeaisummary.util.ai;

import org.example.youtubeaisummary.dto.youtube.SubtitleDTO;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    private static final String SYSTEM_PROMPT = """
            You are an expert video summarizer. Your job is to summarize a YouTube video using its English auto-generated transcript, which may contain minor errors.

            Instructions:
            1. Follow the chronological order of the video.
            2. Divide the summary into sections using clear ## Markdown headings.
            3. Correct and clarify errors in the transcript contextually.
            4. Preserve specific details:
               - Notable quotes (in quotes)
               - Examples, metaphors
               - Exact statistics (e.g. numbers, dates)
            5. Avoid repetition, filler phrases, and hallucinations.
            6. Output must be in **Korean** using **clean, information-focused tone** suitable for structured UI.
            7. Format the result using `##` headings and inline formatting. Use emojis like 📌, 💬, 📊 in place of bullet points to improve readability and UI rendering.
            """;

    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(SubtitleDTO subtitles) {
        return """
                Transcript:
                %s

                Task:
                Summarize the transcript above into a structured Markdown summary in Korean.

                Instructions:
                - Follow the video’s timeline (chronological order).
                - Use `##` headings for each section.
                - Use emojis (e.g., 📌, 💬, 📊) at the start of each bullet point instead of -, *, or •.
                - Keep output concise, informative, and easy to scan.
                - Only return the final summary in Markdown. No extra explanation.
                """.formatted(subtitles.getSubtitles());
    }
}