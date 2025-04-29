package org.example.youtubeaisummary.util.youtube;

import java.util.Map;
import java.util.Optional;

public interface SubtitlePriorityPolicy {
    Optional<Choice> choose(Map<String, Boolean> subs);

    record Choice(String lang, boolean manual) {
    }
}