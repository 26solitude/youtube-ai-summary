package org.example.youtubeaisummary.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class DuplicateRemovalService {

    // 문장 간 중복을 판단할 때 사용하는 Jaccard 임계치
    private final double similarityThreshold;

    public DuplicateRemovalService() {
        // 필요에 따라 0.3~0.5 등으로 조정 가능
        this(0.5);
    }

    public DuplicateRemovalService(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * (1) 긴 문자열 상태에서 "연속 구절" 제거
     * - KSS로 문장 분리하기 전에 중복 제거를 실행
     */
    public String removeAllRepeatedSequencesGlobal(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        // 문장부호를 공백으로 치환
        text = text.replaceAll("[.,!?…]", " ");
        // 공백 정리
        text = text.replaceAll("\\s+", " ").trim();

        // 여러 번 반복 적용
        String prev;
        do {
            prev = text;
            text = text.replaceAll(
                    "(?i)([\\p{L}0-9]+(?:\\s+[\\p{L}0-9]+)*)(\\s+\\1)+",
                    "$1"
            );
        } while (!text.equals(prev));

        return text;
    }

    /**
     * (2) KSS로 문장 분리된 후, "문장 간 중복"을 제거
     * - Jaccard 유사도 기준
     */
    public ArrayList<String> removeDuplicates(ArrayList<String> sentences) {
        ArrayList<String> uniqueSentences = new ArrayList<>();
        int windowSize = 5;  // 최근 5개의 문장과만 비교

        for (String sentence : sentences) {
//            System.out.println("[DEBUG] Checking new sentence:\n" + sentence);

            boolean isDuplicate = false;
            int startIndex = Math.max(0, uniqueSentences.size() - windowSize);
            for (int i = startIndex; i < uniqueSentences.size(); i++) {
                String existing = uniqueSentences.get(i);
                double sim = computeJaccardSimilarity(sentence, existing);

                if (sim > similarityThreshold) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                uniqueSentences.add(sentence);
            }
        }

        return uniqueSentences;
    }

    /**
     * Jaccard 유사도 (단어 단위)
     */
    private double computeJaccardSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;

        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();

        for (String w : words1) {
            if (!w.isBlank()) set1.add(w.toLowerCase());
        }
        for (String w : words2) {
            if (!w.isBlank()) set2.add(w.toLowerCase());
        }

        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0;
        }

        // 교집합
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // 합집합
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}
