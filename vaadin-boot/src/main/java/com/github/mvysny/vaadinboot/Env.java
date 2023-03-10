package com.github.mvysny.vaadinboot;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class Env {
    private static final Logger log = LoggerFactory.getLogger(Env.class);
    private Env() {}

    private static boolean detectProductionMode() {
        // try checking for flow-server-production-mode.jar on classpath
        final String probe = "META-INF/maven/com.vaadin/flow-server-production-mode/pom.xml";
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader.getResource(probe) != null) {
            log.info("Vaadin production mode is on: META-INF/maven/com.vaadin/flow-server-production-mode/pom.xml is present");
            return true;
        }

        // Gradle plugin doesn't add flow-server-production-mode.jar to production build. Try loading flow-build-info.json instead.
        final URL flowBuildInfoJson = classLoader.getResource("META-INF/VAADIN/config/flow-build-info.json");
        if (flowBuildInfoJson != null) {
            try {
                final String json = IOUtils.toString(flowBuildInfoJson, StandardCharsets.UTF_8);
                if (json.contains("\"productionMode\": true")) {
                    log.info("Vaadin production mode is on: META-INF/VAADIN/config/flow-build-info.json contains '\"productionMode\": true'");
                    return true;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            log.info("Vaadin production mode is off: META-INF/VAADIN/config/flow-build-info.json doesn't contain '\"productionMode\": true'");
            return false;
        }
        log.info("Vaadin production mode is off: META-INF/VAADIN/config/flow-build-info.json is missing");
        return false;
    }

    /**
     * Detects whether Vaadin is configured to run in production mode or not.
     */
    public static final boolean isVaadinProductionMode = detectProductionMode();

    /**
     * Detects the web root folder, used to serve static content.
     * @return resource serving web root.
     * @throws MalformedURLException
     */
    @NotNull
    static Resource findWebRoot() throws MalformedURLException {
        // don't look up directory as a resource, it's unreliable: https://github.com/eclipse/jetty.project/issues/4173#issuecomment-539769734
        // instead we'll look up the /webapp/ROOT and retrieve the parent folder from that.
        final URL f = VaadinBoot.class.getResource("/webapp/ROOT");
        if (f == null) {
            throw new IllegalStateException("Invalid state: the resource /webapp/ROOT doesn't exist, has webapp been packaged in as a resource?");
        }
        final String url = f.toString();
        if (!url.endsWith("/ROOT")) {
            throw new RuntimeException("Parameter url: invalid value " + url + ": doesn't end with /ROOT");
        }
        log.info("/webapp/ROOT is " + f);

        // Resolve file to directory
        final URL webRoot = new URL(url.substring(0, url.length() - 5));
        log.info("WebRoot is " + webRoot);
        return Resource.newResource(webRoot);
    }

    /**
     * Returns a short string containing Java version and OS info, for example
     * <code>Java Amazon.com Inc. 17.0.5, OS amd64 Linux 5.19.0-35-generic</code>
     * @return short host info
     */
    @NotNull
    static String dumpHost() {
        return "Java " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + ", OS " + System.getProperty("os.arch") + " " + System.getProperty("os.name") + " " + System.getProperty("os.version");
    }
}
