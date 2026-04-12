package com.example.devops;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Commits a file to GitHub using the GitHub Contents REST API.
 * Requires GITHUB_TOKEN env var with repo write scope.
 * Files are stored under the  code/  folder in the repository.
 */
public class GitHubService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Creates or updates  code/{filename}  in the configured GitHub repo.
     * Returns the full commit SHA on success.
     */
    public String commitFile(String filename, String content, String commitMessage) throws Exception {
        String path = "code/" + filename;
        String apiUrl = "https://api.github.com/repos/"
                + Config.GITHUB_OWNER + "/" + Config.GITHUB_REPO
                + "/contents/" + path;

        // GitHub requires the existing file's SHA when updating
        String existingSha = getFileSha(apiUrl);

        JsonObject body = new JsonObject();
        body.addProperty("message", commitMessage);
        body.addProperty("content", Base64.getEncoder().encodeToString(content.getBytes("UTF-8")));
        body.addProperty("branch", Config.GITHUB_BRANCH);
        if (existingSha != null) {
            body.addProperty("sha", existingSha);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "token " + Config.GITHUB_TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            return json.getAsJsonObject("commit").get("sha").getAsString();
        }

        if (response.statusCode() == 401) {
            throw new Exception("GitHub token is invalid or expired. Update GITHUB_TOKEN in .env and restart backend.");
        }

        if (response.statusCode() == 403) {
            throw new Exception("GitHub token lacks write access. Grant Contents: Read and write for repo "
                    + Config.GITHUB_OWNER + "/" + Config.GITHUB_REPO + ".");
        }

        // Surface a clear error message
        String errorBody = response.body();
        try {
            JsonObject err = gson.fromJson(errorBody, JsonObject.class);
            if (err.has("message")) errorBody = err.get("message").getAsString();
        } catch (Exception ignored) {}

        throw new Exception("GitHub API returned " + response.statusCode() + ": " + errorBody);
    }

    /** Returns the blob SHA of an existing file, or null if it does not exist yet. */
    private String getFileSha(String apiUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "token " + Config.GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), JsonObject.class).get("sha").getAsString();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
