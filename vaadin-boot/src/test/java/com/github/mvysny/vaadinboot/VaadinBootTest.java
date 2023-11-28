package com.github.mvysny.vaadinboot;

import com.vaadin.flow.server.VaadinServlet;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VaadinBootTest {
    /**
     * Mock env variables. {@link Env#ENV_RESOLVER} is configured to honor settings from this map.
     */
    @NotNull
    private final Map<String, String> env = new HashMap<>();

    @BeforeEach
    public void clearPropertiesVariables() {
        System.clearProperty("server.address");
        System.clearProperty("server.port");
        System.clearProperty("server.servlet.context-path");
        Env.ENV_RESOLVER = env::get;
    }

    @Test
    public void smoke() {
        new VaadinBoot();
    }

    @Test
    public void testDefaultPort() {
        assertEquals(8080, new VaadinBoot().port);
    }

    @Test
    public void testPortParsedCorrectly() {
        assertEquals(9090, new VaadinBoot().setPort(9090).port);
    }

    @Test
    public void testPortParsedCorrectlyFromSystemProperty() {
        System.setProperty("server.port", "8081");
        assertEquals(8081, new VaadinBoot().port);
        // manual config takes precedence
        assertEquals(9090, new VaadinBoot().setPort(9090).port);
    }

    @Test
    public void testPortParsedCorrectlyFromEnv() {
        env.put("SERVER_PORT", "8082");
        assertEquals(8082, new VaadinBoot().port);
        // system property takes precedence
        System.setProperty("server.port", "8081");
        assertEquals(8081, new VaadinBoot().port);
        // manual config takes precedence
        assertEquals(9090, new VaadinBoot().setPort(9090).port);
    }

    @Test
    public void testDefaultHost() {
        assertNull(new VaadinBoot().hostName);
    }

    @Test
    public void testHostParsedCorrectly() {
        assertEquals("foo", new VaadinBoot().listenOn("foo").hostName);
        assertEquals("localhost", new VaadinBoot().localhostOnly().hostName);
    }

    @Test
    public void testHostParsedCorrectlyFromSystemProperty() {
        System.setProperty("server.address", "bar");
        assertEquals("bar", new VaadinBoot().hostName);
        // manual config takes precedence
        assertEquals("localhost", new VaadinBoot().localhostOnly().hostName);
    }

    @Test
    public void testHostParsedCorrectlyFromEnv() {
        env.put("SERVER_ADDRESS", "bar2");
        assertEquals("bar2", new VaadinBoot().hostName);
        // system property takes precedence
        System.setProperty("server.address", "bar");
        assertEquals("bar", new VaadinBoot().hostName);
        // manual config takes precedence
        assertEquals("localhost", new VaadinBoot().localhostOnly().hostName);
    }

    @Test
    public void testDefaultContextRoot() {
        assertEquals("/", new VaadinBoot().contextRoot);
    }

    @Test
    public void testContextRootParsedCorrectly() {
        assertEquals("/foo", new VaadinBoot().withContextRoot("/foo").contextRoot);
    }

    @Test
    public void testContextRootParsedCorrectlyFromSystemProperty() {
        System.setProperty("server.servlet.context-path", "/bar");
        assertEquals("/bar", new VaadinBoot().contextRoot);
        // manual config takes precedence
        assertEquals("/", new VaadinBoot().withContextRoot("/").contextRoot);
    }

    @Test
    public void testContextRootParsedCorrectlyFromSystemEnv() {
        env.put("SERVER_SERVLET_CONTEXT-PATH", "/foo");
        assertEquals("/foo", new VaadinBoot().contextRoot);
        // system property takes precedence
        System.setProperty("server.servlet.context-path", "/bar");
        assertEquals("/bar", new VaadinBoot().contextRoot);
        // manual config takes precedence
        assertEquals("/", new VaadinBoot().withContextRoot("/").contextRoot);
    }

    @Test
    public void scanTestClasspathModifiesWebAppConfig() throws Exception {
        final WebAppContext ctx = new VaadinBoot().scanTestClasspath().createWebAppContext();
        assertEquals(".*\\.jar|.*/classes/.*|.*/test-classes/.*", ctx.getAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern"));
    }

    @WebServlet(name = "myservlet", initParams = @WebInitParam(name = "foo", value = "bar"))
    public static class MyServlet extends VaadinServlet {}

    @Test
    public void smokeCustomServlet() throws Exception {
        new VaadinBoot().withServlet((Class<? extends Servlet>) MyServlet.class);
    }
}
