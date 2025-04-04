package org.example.youtubeaisummary.controller;

import org.example.youtubeaisummary.dto.youtube.VideoInfo;
import org.example.youtubeaisummary.service.youtube.YoutubeClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class YoutubeAISummaryController {
    private final YoutubeClient youtubeClient;
    private final ChatClient chatClient;

    public YoutubeAISummaryController(YoutubeClient youtubeClient, ChatClient.Builder chatClientBuilder) {
        this.youtubeClient = youtubeClient;
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/api/video-info")
    public Map<String, String> getVideoInfo(@RequestParam String url) {
        // 1) 유튜브에서 제목·자막 정보 가져오기
        VideoInfo info = youtubeClient.fetchVideoInfo(url);

        // 2) 프롬프트 - 시스템(역할/요청사항)
        String systemPrompt = """
                당신은 다양한 주제(뉴스·정보·자기계발·동기부여 등)를 다루는 유튜브 영상을
                핵심만 뽑아 '카드뉴스' 형태로 정리·요약하는 전문 작가입니다.

                목표(Objective):
                - 아래에 제공되는 유튜브 영상 정보(제목과 자막 또는 대본 전문)를 바탕으로
                  오타·중복·불필요한 대화·배경음 안내 등을 제거하고,
                  핵심 메시지나 주요 정보가 잘 드러나는 요약본을 작성해주세요.

                요청사항(Requirements):
                1) 부정확하거나 자동 생성 특유의 오타·중복은 제거해 주세요.
                2) 영상 맥락에 맞지 않는 표현은 자연스럽게 수정·보정하세요.
                3) 영상의 목적(뉴스·정보 전달 / 자기계발·동기부여 등)에 따라 글톤을 적용해 주세요.
                   - 뉴스·정보성 영상: 사실관계와 수치 중심, 객관적·간결한 문체
                   - 자기계발·동기부여 영상: 핵심 교훈이나 실천 포인트 중심, 긍정적인 어조
                4) 카드뉴스 형식(또는 이에 준하는 요약본)으로 작성해 주세요:
                   (1) 메인 헤드라인
                   (2) 핵심 요약 포인트 (3~6개 내외)
                   (3) 결론·조언 또는 전망
                5) 내용은 간결하고 쉽게 정리하되, 주요 정보(수치·이름·장소 등)는 정확히 포함해 주세요.
                6) 최종 출력은 마크다운 문법(번호 목록·강조 등)을 활용해 가독성을 높여 주세요.
                """;

        // 3) 프롬프트 - 사용자 메시지(제목 & 자막)
        String userPrompt = "영상 제목: " + info.getTitle()
                + "\n\n"
                + "자막(또는 대본):\n"
                + info.getSubtitles();

        // 4) ChatClient로 요청 -> 요약 결과(ChatResponse)
        ChatResponse chatResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .chatResponse();

        // 5) ChatResponse에서 텍스트만 추출 (요약 결과)
        String summarizedText = chatResponse.getResult().getOutput().getText();

        // 6) LinkedHashMap을 사용해 "title"과 "summary"만 반환
        Map<String, String> result = new LinkedHashMap<>();
        result.put("title", info.getTitle());
        result.put("summary", summarizedText);

        return result;
    }
}