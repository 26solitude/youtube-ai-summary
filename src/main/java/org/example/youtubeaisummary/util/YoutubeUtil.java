package org.example.youtubeaisummary.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUtil {
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("v=([^&]+)");

    public static String extractVideoId(String url) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("유효하지 않은 YouTube URL: " + url);
        }
    }
}