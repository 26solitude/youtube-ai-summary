package org.example.youtubeaisummary.service.youtube;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DuplicateRemovalService {

    private final double similarityThreshold;

    public DuplicateRemovalService() {
        this(0.5);
    }

    public DuplicateRemovalService(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * (1) "스택 기반" + "최대 블록 길이 6" 연속 반복 제거
     * - 전체 텍스트를 단어로 쪼갠 뒤,
     * 왼->오른쪽으로 순회하며, 스택에 push하고
     * push 직후 "맨 위 2블록"이 동일하면 pop
     */
    public String removeAllRepeatedSequencesGlobal(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        // 1) 구두점 -> 공백, 연속 공백 정리
        text = text.replaceAll("[.,!?…]", " ");
        text = text.replaceAll("\\s+", " ").trim();
        if (text.isEmpty()) return text;

        // 2) 토큰 리스트
        String[] arr = text.split(" ");  // 단어 단위
        // "스택" 역할로 LinkedList<String> (아래선 ArrayList도 가능)
        List<String> stack = new LinkedList<>();

        // 3) 왼->오른쪽으로 차례대로
        for (String token : arr) {
            // push
            stack.add(token);

            // 3-1) "연속 블록" 제거를 시도
            //   - 가장 긴 블록(6)부터 1까지,
            //     "맨 위 블록 vs 바로 아래 블록"이 같으면 제거
            boolean done = false;
            while (!done) {
                done = true;  // 변동 없으면 while 탈출
                for (int L = 6; L >= 1; L--) {
                    if (stack.size() >= 2 * L) {
                        // 맨 위 L개, 그 아래 L개 비교
                        if (isTop2BlocksSame(stack, L)) {
                            // 중복이면 "맨 위 L개" pop
                            removeTop(stack, L);
                            // pop 후 다시 한 번 가장 긴 블록부터 검사해봐야 함
                            done = false;
                            break;
                        }
                    }
                }
            }
        }

        // 4) 결과
        return String.join(" ", stack);
    }

    /**
     * stack의 맨 위 2*L개 중, [top-2L .. top-L-1] 과 [top-L .. top-1] 구간이 동일한가
     */
    private boolean isTop2BlocksSame(List<String> stack, int L) {
        int n = stack.size();
        // 앞 구간 시작: n - 2L
        // 뒤 구간 시작: n - L
        for (int i = 0; i < L; i++) {
            String a = stack.get(n - 2 * L + i);
            String b = stack.get(n - L + i);
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * stack의 top에서 L개 pop
     */
    private void removeTop(List<String> stack, int L) {
        int n = stack.size();
        // n - 1, n - 2, ... n - L 순서로 제거
        for (int i = 0; i < L; i++) {
            stack.remove(n - 1 - i);
        }
    }

    /**
     * (2) KSS로 문장 분리된 후, "문장 간 중복"을 제거 (기존 로직)
     * - Jaccard 유사도 기준
     */
    public ArrayList<String> removeDuplicates(ArrayList<String> sentences) {
        ArrayList<String> uniqueSentences = new ArrayList<>();
        int windowSize = 5;  // 최근 5개의 문장과만 비교

        for (String sentence : sentences) {
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