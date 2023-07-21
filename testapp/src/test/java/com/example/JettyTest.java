package com.example;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Actually starts up Jetty. DON'T USE FOR TESTING OF YOUR APPS: see {@link MainViewTest} instead.
 */
public class JettyTest {
    private VaadinBoot vaadinBoot;

    @BeforeEach
    public void startJetty() throws Exception {
        assertFalse(Bootstrap.initialized);
        vaadinBoot = new VaadinBoot().setPort(44312).localhostOnly();
        vaadinBoot.start();
    }

    @AfterEach
    public void stopJetty() throws Exception {
        vaadinBoot.stop("tests");
        assertFalse(Bootstrap.initialized);
    }

    @Test
    public void testAppIsUp() throws Exception {
        // make sure Bootstrap was called
        assertTrue(Bootstrap.initialized);

        // make sure something is running on port 44312
        final HttpClient client = HttpClient.newBuilder().build();
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:44312")).build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(response + " failed: " + response.body());
        }
        System.out.println("Vaadin responded with: " + response + " " + response.body());
    }
}
