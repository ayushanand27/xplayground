package com.example.devops;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    void messageConstantIsCorrect() {
        // Test the constant exists and has expected value
        assertNotNull(App.MESSAGE);
        assertEquals("DevOps Pipeline Working", App.MESSAGE);
    }

    @Test
    void messageIsNotEmpty() {
        // Additional sanity check
        assertEquals(false, App.MESSAGE.isEmpty());
    }

    @Test
    void metricsEndpointExposesHttpRequestsCounter() throws Exception {
        URL url = new URL("http://localhost:8800/metrics");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);

        assertEquals(200, connection.getResponseCode());

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line).append('\n');
            }
        }

        assertTrue(body.toString().contains("http_requests_total"));
    }
}
