package com.example;

import com.github.mvysny.vaadinboot.VaadinBoot;
import com.github.mvysny.vaadinboot.common.Env;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Actually starts up Tomcat. DON'T USE FOR TESTING OF YOUR APPS: see {@link MainViewTest} instead.
 */
public class TomcatTest {
    private static VaadinBoot vaadinBoot;

    @BeforeAll
    public static void startTomcat() throws Exception {
        assertFalse(Bootstrap.initialized);
        vaadinBoot = new VaadinBoot().withPort(44312).localhostOnly();
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

    /**
     * Regression guard: a <code>-Pvaadin.productionMode</code> build must boot the app in
     * production mode, and a dev build in dev mode. The expected value is passed in by the
     * build via the <code>expectedVaadinProductionMode</code> system property.
     */
    @Test
    public void runsInModeSelectedByBuild() {
        assertEquals(
                Boolean.parseBoolean(System.getProperty("expectedVaadinProductionMode")),
                Env.isVaadinProductionMode);
    }
}
