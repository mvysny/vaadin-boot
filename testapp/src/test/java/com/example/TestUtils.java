package com.example;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TestUtils {
    @NotNull
    public static String wget(@NotNull String url) throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newBuilder().build();
        final HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10)) // 3 seconds isn't enough for Windows in GitHub Actions. Increase to 10s
                .build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(response + " failed: " + response.body());
        }
        final String body = response.body();
        return body;
    }
}
