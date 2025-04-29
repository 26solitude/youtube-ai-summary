package org.example.youtubeaisummary.service.youtube;


import org.example.youtubeaisummary.exception.youtube.YoutubeErrorCode;
import org.example.youtubeaisummary.exception.youtube.YoutubeExtractionException;
import org.example.youtubeaisummary.util.youtube.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SubtitleFetcher {
    private static final Logger log = LoggerFactory.getLogger(SubtitleFetcher.class);
    private static final String TMP = "temp";

    private final SentenceSplitterService splitter;
    private final DuplicateRemovalService duplicateRemover;
    private final SubtitlePriorityPolicy policy = new DefaultSubtitlePriorityPolicy();

    public SubtitleFetcher(SentenceSplitterService s, DuplicateRemovalService d) {
        this.splitter = s;
        this.duplicateRemover = d;
    }

    public String fetchSubtitle(MetadataFetcher.VideoMeta meta, String url) {
        SubtitlePriorityPolicy.Choice choice = policy.choose(meta.subtitles())
                .orElseThrow(() -> new YoutubeExtractionException(YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED, "no subs"));
        return download(url, choice);
    }

    private String download(String url, SubtitlePriorityPolicy.Choice c) {
        try {
            String vid = YoutubeUtil.extractVideoId(url);
            String opt = c.manual() ? "--write-sub" : "--write-auto-sub";
            String out = TMP + File.separator + vid;
            ProcessExecutor.ProcessResult result = ProcessExecutor.executeCommand(
                    "yt-dlp", opt, "--sub-lang", c.lang(), "--skip-download",
                    "--sub-format", "vtt", "-o", out, url);
            if (result.getExitCode() != 0) {
                throw new YoutubeExtractionException(
                        YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED,
                        String.format(YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED.getMessage(), result.getExitCode()));
            }
            Path vtt = Path.of(out + "." + c.lang() + ".vtt");
            if (!Files.exists(vtt)) {
                throw new YoutubeExtractionException(
                        YoutubeErrorCode.SUBTITLE_FILE_NOT_FOUND,
                        String.format(YoutubeErrorCode.SUBTITLE_FILE_NOT_FOUND.getMessage(), vtt.toString()));
            }
            String raw = Files.readString(vtt, StandardCharsets.UTF_8);
            Files.delete(vtt);
            String cleaned = VttCleaner.clean(raw);
            String noRepeat = duplicateRemover.removeAllRepeatedSequencesGlobal(cleaned);
            var sentences = splitter.splitSentences(noRepeat);
            var unique = duplicateRemover.removeDuplicates(sentences);
            return String.join(". ", unique);
        } catch (Exception e) {
            throw new YoutubeExtractionException(
                    YoutubeErrorCode.SUBTITLE_EXTRACTION_FAILED,
                    "Subtitle download processing error: " + e.getMessage());
        }
    }
}