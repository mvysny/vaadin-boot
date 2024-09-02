package com.example;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Actually starts up Tomcat. DON'T USE FOR TESTING OF YOUR APPS: see {@link MainViewTest} instead.
 */
public class TomcatTest {
    private static VaadinBoot vaadinBoot;

    @BeforeAll
    public static void startTomcat() throws Exception {
        assertFalse(Bootstrap.initialized);
        vaadinBoot = new VaadinBoot().setPort(44312).localhostOnly();
        vaadinBoot.start();
    }

    @AfterAll
    public static void stopTomcat() throws Exception {
        vaadinBoot.stop("tests");
        assertFalse(Bootstrap.initialized);
    }

    @Test
    public void testAppIsUp() throws Exception {
        // make sure Bootstrap was called
        assertTrue(Bootstrap.initialized);

        // make sure something is running on port 44312
        final String response = TestUtils.wget("http://localhost:44312");
        assertTrue(response.contains("window.Vaadin"), response);
    }

    @Test
    public void testStaticFilesServedFromWebappFolder() throws Exception {
        final String response = TestUtils.wget("http://localhost:44312/ROOT");
        assertEquals("Don't delete this file; see Main.java for details.", response.trim());
    }
}
