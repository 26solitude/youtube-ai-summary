package org.example.youtubeaisummary.util.youtube;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SubtitleProcessor {

    /*──────── 기본 파라미터 ────────*/
    private static final double  SIM_THRESHOLD = 0.80;
    private static final int     WINDOW_SIZE   = 8;          // ★ 5→8 : 긴 블록 반복 컷
    private static final double  GLOBAL_SIM    = 0.90;       // ★ 전역(긴 문장) 유사도
    private static final LevenshteinDistance LEV = new LevenshteinDistance();

    /* filler(접속사·의미단어 제외) */
    private static final Set<String> FILLER = Set.of(
            "uh","um","like","you know","i mean","sort of","kind of",
            "basically","actually","right","well","just","literally",
            "oh","ah","yes","yeah","okay","ok","mhm","hm","hello","hi",
            "'s"                                               // ★ 추가 : 잘린 's
    );

    private static final Set<String> STOP = Set.of(
            "great","sure","yeah","yes","okay","ok","right","mhm","hm","oh","ah","wow"
    );

    /*───────────────────────────────────────────────────────────*/
    public static String processSubtitleContent(String raw) {
        if (raw == null || raw.isBlank()) return "";

        /* 0. 전역 정규화 */
        String txt = raw
                .replaceAll("(?<!\\d),(?!\\d)", " ")                 // 쉼표→공백
                .replaceAll("(?i)WEBVTT", "")
                .replaceAll("\\d{2}:\\d{2}:\\d{2}\\.\\d{3} --> .*", "")
                .replaceAll("<.*?>", "")
                .replaceAll("([.?!…])([\"')»”])?\\s+", "$1$2\n")     // 구두점 뒤 개행
                .replaceAll("\\n{2,}", "\n")
                .replaceAll("\\s{2,}", " ");                         // ★ 중복공백 축소

        /* 1. 문장 배열 */
        List<String> lines = Arrays.stream(txt.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        /* 2. 정제 + 중복 필터 */
        Set<String> globalSeen = new HashSet<>();
        List<String> out       = new ArrayList<>();

        for (String l : lines) {
            String c = clean(l);
            if (c.isBlank()) continue;

            /* 병합·보정 */
            c = postProcessLine(c, out);
            if (c.isBlank()) continue;

            /* 2‑a. 전역 exact 중복 */
            String keyExact = c.replaceAll("\\s+", " ");          // ★ 공백1개로 통일
            if (!globalSeen.add(keyExact)) continue;

            /* 2‑b. 긴 문장 전역 유사도 */
            if (keyExact.split(" ").length >= 12 &&
                    globalSeen.stream().anyMatch(k -> similarity(k, keyExact) >= GLOBAL_SIM))
                continue;

            /* 2‑c. 최근 WINDOW_SIZE 유사도 */
            if (keyExact.split(" ").length >= 5) {
                String cmp = keyExact.replaceAll("[^a-z0-9\\s]", "");
                boolean dup = out.stream()
                        .skip(Math.max(0, out.size() - WINDOW_SIZE))
                        .anyMatch(pr -> similarity(
                                pr.replaceAll("[^a-z0-9\\s]", ""), cmp) >= SIM_THRESHOLD);
                if (dup) continue;
            }
            out.add(keyExact);
        }
        return String.join("\n", out).trim();
    }

    /*──────── line 단위 정제 ────────*/
    private static String clean(String s) {
        String r = s.toLowerCase().trim();

        /* a. filler 제거 */
        for (String f : FILLER) {
            r = r.replaceAll("(?i)\\b" + Pattern.quote(f) + "\\b[,.]*", "");
        }

        /* b. 관용구 서두 컷 */
        r = r.replaceFirst("(?i)^\\s*(i\\s+think|you\\s+know|in\\s+fact|basically|well|so|actually|just|kind\\s+of|sort\\s+of)[\\s,:;-]*", "");

        /* c. 2단어 이하 / 동사없는 4단어 이하 컷 */
        String[] tok = r.split("\\s+");
        if (tok.length <= 2) return "";
        if (tok.length <= 4 && !r.matches(".*\\b(is|are|am|was|were|be|have|has|had|do|does|did)\\b.*"))
            return "";

        /* d. 짧은 의문문 컷 */
        if (r.endsWith("?") && tok.length <= 5) return "";

        /* e. 단어 반복+구두점 정리 */
        r = r.replaceAll("(?i)\\b(\\w+)(\\s+\\1\\b)+", "$1")
                .replaceAll("\\s*[.?!,]{2,}", ".")
                .replaceAll("\\s+\\.", ".")
                .replaceAll("\\s{2,}", " ")
                .trim();

        /* f. STOP phrase 단독 컷 */
        if (r.split("\\s+").length <= 3 && STOP.contains(r)) return "";

        /* g. 고립 접속사 단독 컷 */
        if (r.matches("(?i)^(or|and|so|but)$")) return "";

        return r;
    }

    /*──────── 한 줄 후처리 & 병합 ────────*/
    private static String postProcessLine(String r, List<String> out) {

        /* g. 의미 없는 단어 단독줄 컷 */
        if (r.matches("(?i)^(in|of|for|to)$")) return "";

        /* h. 접속사 사이 이중공백 → 1공백 */
        r = r.replaceAll("\\s+(so|and|but)\\s+", " $1 ");

        /* i. 앞줄과 병합 : in|of|for 로 시작하면 앞줄 뒤에 붙임 */
        if (!out.isEmpty() && r.matches("(?i)^(in|of|for)\\b.*")) {
            out.set(out.size() - 1, out.get(out.size() - 1) + " " + r);
            return "";
        }
        return r;
    }

    /*──────── Levenshtein 유사도 ────────*/
    private static double similarity(String a, String b) {
        int len = Math.max(a.length(), b.length());
        return len == 0 ? 1.0 : 1.0 - (double) LEV.apply(a, b) / len;
    }
}