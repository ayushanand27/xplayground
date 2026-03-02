package com.example.devops;

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

    public static final String GITHUB_TOKEN  = env("GITHUB_TOKEN",  "");
    public static final String GITHUB_OWNER  = env("GITHUB_OWNER",  "ayushanand27");
    public static final String GITHUB_REPO   = env("GITHUB_REPO",   "xplayground");
    public static final String GITHUB_BRANCH = env("GITHUB_BRANCH", "main");

    public static final String JENKINS_URL   = env("JENKINS_URL",   "http://localhost:8080");
    public static final String JENKINS_USER  = env("JENKINS_USER",  "ayush_anand_2327");
    public static final String JENKINS_TOKEN = env("JENKINS_TOKEN", "118d1fbc6b1e76be5f8b246e33691c2995");
    public static final String JENKINS_JOB   = env("JENKINS_JOB",   "xpg");

    public static boolean isGitHubConfigured() {
        return !GITHUB_TOKEN.isEmpty();
    }

    public static boolean isJenkinsConfigured() {
        return !JENKINS_USER.isEmpty() && !JENKINS_TOKEN.isEmpty();
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
