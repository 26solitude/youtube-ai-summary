package org.example.youtubeaisummary.service.ai;

import org.example.youtubeaisummary.dto.ai.SummaryResult;
import org.example.youtubeaisummary.dto.youtube.VideoInfo;
import org.example.youtubeaisummary.exception.youtube.YoutubeExtractionException;
import org.example.youtubeaisummary.util.ai.PromptBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class AISummaryService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;

    public AISummaryService(ChatClient.Builder chatClientBuilder, PromptBuilder promptBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
    }

    public SummaryResult generateSummary(VideoInfo info) {
        try {
            String systemPrompt = promptBuilder.getSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(info);

            ChatResponse chatResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .chatResponse();

            String summarizedText = chatResponse.getResult().getOutput().getText();
            return new SummaryResult(info.getTitle(), summarizedText);
        } catch (Exception e) {
            throw new YoutubeExtractionException(null, "AI 요약 생성 오류: " + e.getMessage());
        }
    }
}
