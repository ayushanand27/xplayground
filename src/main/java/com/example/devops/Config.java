package com.example.devops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads configuration from environment variables.
 *
 * Set these before starting the app:
 *   GITHUB_TOKEN  — your GitHub Personal Access Token (needs repo write scope)
 *   GITHUB_OWNER  — your GitHub username (default: ayushanand27)
 *   GITHUB_REPO   — your repository name  (default: xplayground)
 *   GITHUB_BRANCH — branch to commit to   (default: main)
 *   JENKINS_URL   — Jenkins base URL      (default: http://localhost:8080)
 *   JENKINS_USER  — Jenkins username
 *   JENKINS_TOKEN — Jenkins API token (from: Jenkins → Your User → Configure → API Token)
 *   JENKINS_JOB   — Jenkins job name      (default: xpg)
 *
 * Windows PowerShell example:
 *   $env:GITHUB_TOKEN="ghp_xxxx"
 *   $env:JENKINS_USER="admin"
 *   $env:JENKINS_TOKEN="11xxxx"
 *   java -jar target\devops-pipeline-app-1.0.0.jar
 */
public class Config {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    public static final String GITHUB_TOKEN  = env("GITHUB_TOKEN",  "");
    public static final String GITHUB_OWNER  = env("GITHUB_OWNER",  "ayushanand27");
    public static final String GITHUB_REPO   = env("GITHUB_REPO",   "xplayground");
    public static final String GITHUB_BRANCH = env("GITHUB_BRANCH", "main");

    public static final String JENKINS_URL   = env("JENKINS_URL",   "http://localhost:8080");
    public static final String JENKINS_USER  = env("JENKINS_USER",  "");
    public static final String JENKINS_TOKEN = env("JENKINS_TOKEN", "");
    public static final String JENKINS_JOB   = env("JENKINS_JOB",   "xpg");

    public static boolean isGitHubConfigured() {
        return !GITHUB_TOKEN.isEmpty();
    }

    public static boolean isJenkinsConfigured() {
        return !JENKINS_USER.isEmpty() && !JENKINS_TOKEN.isEmpty();
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value != null) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }

        String fromDotEnv = DOT_ENV.get(key);
        if (fromDotEnv != null) {
            String trimmed = fromDotEnv.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }

        return defaultValue;
    }

    private static Map<String, String> loadDotEnv() {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            return Collections.emptyMap();
        }

        Map<String, String> values = new HashMap<>();
        try {
            for (String rawLine : Files.readAllLines(envPath)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();

                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                }

                if (!key.isEmpty()) {
                    values.put(key, value);
                }
            }
        } catch (IOException ignored) {
            // Fall back to environment/default values.
        }

        return values;
    }
}
