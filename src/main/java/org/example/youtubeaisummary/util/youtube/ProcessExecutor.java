package org.example.youtubeaisummary.util.youtube;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

public class ProcessExecutor {

    public static ProcessResult executeCommand(String... command)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), Charset.forName("MS949")))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw ie;
        }

        return new ProcessResult(exitCode, output);
    }


    public static class ProcessResult {
        private final int exitCode;
        private final String output;

        public ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }
    }
}