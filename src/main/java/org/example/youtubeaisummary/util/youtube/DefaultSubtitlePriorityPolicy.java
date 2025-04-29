package org.example.youtubeaisummary.util.youtube;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultSubtitlePriorityPolicy implements SubtitlePriorityPolicy {
    private static final List<Choice> ORDER = List.of(
            new Choice("ko", true), new Choice("en", true),
            new Choice("ko", false), new Choice("en", false));

    public Optional<Choice> choose(Map<String, Boolean> subs) {
        return ORDER.stream().filter(c -> subs.getOrDefault(c.lang(), false) == c.manual()).findFirst();
    }
}
