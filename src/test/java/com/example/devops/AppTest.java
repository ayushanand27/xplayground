package com.example.devops;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.LockSupport;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static spark.Spark.awaitStop;
import static spark.Spark.stop;

public class AppTest {

    private static volatile boolean appStarted = false;
    private static volatile Thread serverThread;
    private static volatile int testPort;

    @BeforeAll
    static void setup() throws Exception {
        testPort = findFreePort();
        System.setProperty("APP_PORT", String.valueOf(testPort));
        startAppOnce();
    }

    @AfterAll
    static void tearDown() {
        if (!appStarted) {
            return;
        }

        try {
            stop();
            awaitStop();
        } catch (Exception ignored) {
            // Best-effort cleanup for test stability across environments.
        } finally {
            appStarted = false;
            System.clearProperty("APP_PORT");
            if (serverThread != null) {
                serverThread.interrupt();
                serverThread = null;
            }
        }
    }

    static void startAppOnce() throws Exception {
        if (appStarted) {
            return;
        }

        serverThread = new Thread(() -> App.main(new String[0]), "app-test-server");
        serverThread.setDaemon(true);
        serverThread.start();

        Instant deadline = Instant.now().plus(Duration.ofSeconds(15));
        while (Instant.now().isBefore(deadline)) {
            try {
                if (healthCheckResponds(testPort)) {
                    appStarted = true;
                    return;
                }
            } catch (Exception ignored) {
                // Retry until the Spark server is ready.
            }
            LockSupport.parkNanos(Duration.ofMillis(250).toNanos());
        }

        throw new IllegalStateException("Backend did not start on port " + testPort + " in time.");
    }

    private static boolean healthCheckResponds(int port) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + port + "/health").openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        return connection.getResponseCode() == 200;
    }

    private static int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    @Test
    void messageConstantIsCorrect() {
        // Test the constant exists and has expected value
        assertNotNull(App.MESSAGE);
        assertEquals("DevOps Pipeline Working", App.MESSAGE);
    }

    @Test
    void messageIsNotEmpty() {
        // Additional sanity check
        assertTrue(!App.MESSAGE.isEmpty());
    }

    @Test
    void metricsEndpointExposesHttpRequestsCounter() throws Exception {
        URL url = new URL("http://localhost:" + testPort + "/metrics");
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
