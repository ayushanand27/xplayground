package com.example.devops;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Interacts with the Jenkins REST API to:
 *   - Trigger a build
 *   - Resolve the queue item to a real build number
 *   - Poll build status
 *   - Fetch console output
 *
 * Requires JENKINS_USER and JENKINS_TOKEN env vars.
 * Get your token at: Jenkins → Your username (top-right) → Configure → API Token → Add new Token
 */
public class JenkinsService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // ── Auth ──────────────────────────────────────────────────────────────────

    private String basicAuth() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((Config.JENKINS_USER + ":" + Config.JENKINS_TOKEN).getBytes());
    }

    /**
     * Jenkins CSRF protection requires a crumb token on POST requests.
     * Returns "CrumbRequestField:crumbValue" or null if crumb issuer is disabled.
     */
    private String getCrumb() {
        try {
            String url = Config.JENKINS_URL + "/crumbIssuer/api/json";
            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Authorization", basicAuth())
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonObject json = gson.fromJson(resp.body(), JsonObject.class);
                return json.get("crumbRequestField").getAsString()
                        + ":" + json.get("crumb").getAsString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── Trigger ───────────────────────────────────────────────────────────────

    /**
     * Triggers a Jenkins build and returns the queue item Location URL.
     * Example return value: "http://localhost:8080/queue/item/7/"
     */
    public String triggerBuild() throws Exception {
        String crumb = getCrumb();
        String url = Config.JENKINS_URL + "/job/" + Config.JENKINS_JOB + "/build";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", basicAuth())
                .POST(HttpRequest.BodyPublishers.noBody());

        if (crumb != null) {
            String[] parts = crumb.split(":", 2);
            builder.header(parts[0], parts[1]);
        }

        HttpResponse<String> resp = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 201) {
            return resp.headers().firstValue("Location").orElse(null);
        }
        throw new Exception("Jenkins returned " + resp.statusCode()
                + " when triggering build. Body: " + resp.body());
    }

    /**
     * Polls the Jenkins queue until the build actually starts.
     * Returns the real build number (e.g. 4).
     * Waits up to 60 seconds; throws if the build never starts.
     */
    public int resolveBuildNumber(String queueItemUrl) throws Exception {
        String apiUrl = queueItemUrl + (queueItemUrl.endsWith("/") ? "" : "/") + "api/json";

        for (int attempt = 0; attempt < 60; attempt++) {
            Thread.sleep(1000);

            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header("Authorization", basicAuth())
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonObject json = gson.fromJson(resp.body(), JsonObject.class);
                if (json.has("executable") && !json.get("executable").isJsonNull()) {
                    return json.getAsJsonObject("executable").get("number").getAsInt();
                }
                // If "why" is present the build is still queued — keep waiting
            }
        }
        throw new Exception("Timed out after 60 s waiting for Jenkins build to start");
    }

    // ── Status ────────────────────────────────────────────────────────────────

    /**
     * Returns a JSON string with current build info:
     *   { "building": true/false, "result": "SUCCESS"|"FAILURE"|"IN_PROGRESS",
     *     "duration": 12345, "buildNumber": 4, "url": "http://..." }
     */
    public String getBuildStatus(int buildNumber) throws Exception {
        String url = Config.JENKINS_URL + "/job/" + Config.JENKINS_JOB
                + "/" + buildNumber + "/api/json";

        HttpResponse<String> resp = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", basicAuth())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200) {
            JsonObject src = gson.fromJson(resp.body(), JsonObject.class);
            JsonObject out = new JsonObject();
            out.addProperty("building",     src.get("building").getAsBoolean());
            out.addProperty("result",       src.get("result").isJsonNull()
                    ? "IN_PROGRESS" : src.get("result").getAsString());
            out.addProperty("duration",     src.get("duration").getAsLong());
            out.addProperty("buildNumber",  buildNumber);
            out.addProperty("url",          src.get("url").getAsString());
            return gson.toJson(out);
        }
        throw new Exception("Jenkins build API returned " + resp.statusCode());
    }

    // ── Console ───────────────────────────────────────────────────────────────

    /** Returns the full plain-text console output of a build. */
    public String getConsoleOutput(int buildNumber) throws Exception {
        String url = Config.JENKINS_URL + "/job/" + Config.JENKINS_JOB
                + "/" + buildNumber + "/consoleText";

        HttpResponse<String> resp = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", basicAuth())
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200) return resp.body();
        throw new Exception("Console API returned " + resp.statusCode());
    }
}
