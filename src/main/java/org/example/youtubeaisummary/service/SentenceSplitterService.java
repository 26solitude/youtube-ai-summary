package org.example.youtubeaisummary.service;

import kss.Kss;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SentenceSplitterService {
    private final Kss kss = new Kss();

    /**
     * 정제된 텍스트를 입력받아 KSS를 사용해 문장 단위로 분리합니다.
     */
    public ArrayList<String> splitSentences(String text) {
        // 기본 옵션: heuristic 알고리즘 사용, 인용부호 및 괄호 처리 사용
        return kss.splitSentences(text);
    }
}