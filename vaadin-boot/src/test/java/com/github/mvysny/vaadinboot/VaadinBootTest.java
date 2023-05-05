package com.github.mvysny.vaadinboot;

import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VaadinBootTest {
    @Test
    public void smoke() {
        new VaadinBoot().withArgs(new String[]{"9090"});
    }

    @Test
    public void testDefaultPort() {
        assertEquals(8080, new VaadinBoot().port);
    }

    @Test
    public void testPortParsedCorrectly() {
        assertEquals(9090, new VaadinBoot().withArgs(new String[]{"9090"}).port);
    }

    @Test
    public void scanTestClasspathModifiesWebAppConfig() throws Exception {
        final WebAppContext ctx = new VaadinBoot().scanTestClasspath().createWebAppContext();
        assertEquals(".*\\.jar|.*/classes/.*|.*/test-classes/.*", ctx.getAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern"));
    }
}
