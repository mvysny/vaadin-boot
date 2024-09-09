package com.example;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

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
        // make sure something is running on port 44312
        final String vaadinPage = TestUtils.wget("http://localhost:44312");
        assertTrue(vaadinPage.contains("window.Vaadin = {Flow: {devServerIsNotLoaded: true}};"), vaadinPage);
        // make sure Bootstrap was called
        assertTrue(Bootstrap.initialized);
        // ensure static content is served
        assertEquals("Don't delete this file; see Main.java for details.", TestUtils.wget("http://localhost:44312/ROOT").trim());
    }
}
