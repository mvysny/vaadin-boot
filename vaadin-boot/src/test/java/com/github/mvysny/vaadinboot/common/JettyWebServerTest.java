package com.github.mvysny.vaadinboot.common;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.util.resource.URLResourceFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class JettyWebServerTest {
    @Test
    public void testFixClasspath() {
        final String cp = System.getProperty("java.class.path");
        try {
            // test crazy classpaths
            System.setProperty("java.class.path", File.pathSeparator);
            JettyWebServer.fixClasspath();
            assertEquals("", System.getProperty("java.class.path"));
            System.setProperty("java.class.path", "a" + File.pathSeparator + "b" + File.pathSeparator + "c");
            JettyWebServer.fixClasspath();
            assertEquals("", System.getProperty("java.class.path"));
            System.setProperty("java.class.path", File.pathSeparator + "a" + File.pathSeparator + "b" + File.pathSeparator + "c" + File.pathSeparator);
            JettyWebServer.fixClasspath();
            assertEquals("", System.getProperty("java.class.path"));

            // classpath with existing entry
            System.setProperty("java.class.path", File.pathSeparator + "src/main/java" + File.pathSeparator);
            JettyWebServer.fixClasspath();
            assertEquals("src/main/java", System.getProperty("java.class.path"));

            // filters out non-existing entries
            System.setProperty("java.class.path", File.pathSeparator + "src/main/java" + File.pathSeparator + "nonexisting");
            JettyWebServer.fixClasspath();
            assertEquals("src/main/java", System.getProperty("java.class.path"));
        } finally {
            System.setProperty("java.class.path", cp);
        }
    }

    @Test
    public void smokeFindWebRoot() throws Exception {
        assertNotNull(JettyWebServer.findWebRoot(new URLResourceFactory()));
    }

    @Test
    public void scanTestClasspathModifiesWebAppConfig() throws Exception {
        final VaadinBoot vaadinBoot = new VaadinBoot().scanTestClasspath();
        final JettyWebServer s = new JettyWebServer();
        s.configure(vaadinBoot);
        final WebAppContext ctx = s.getContext();
        assertEquals(".*\\.jar|.*/classes/.*|.*/test-classes/.*", ctx.getAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern"));
    }
}
