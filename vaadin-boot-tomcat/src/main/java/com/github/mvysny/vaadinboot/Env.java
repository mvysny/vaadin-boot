package com.github.mvysny.vaadinboot;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Environment-related utility functions.
 */
final class Env {
    private static final Logger log = LoggerFactory.getLogger(Env.class);
    private Env() {}

    private static boolean detectProductionMode() {
        // try checking for flow-server-production-mode.jar on classpath
        final String probe = "META-INF/maven/com.vaadin/flow-server-production-mode/pom.xml";
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL probeURL = classLoader.getResource(probe);
        if (probeURL != null) {
            log.info("Vaadin production mode is on: " + probeURL + " is present");
            return true;
        }

        // Gradle plugin doesn't add flow-server-production-mode.jar to production build. Try loading flow-build-info.json instead.
        final URL flowBuildInfoJsonURL = classLoader.getResource("META-INF/VAADIN/config/flow-build-info.json");
        if (flowBuildInfoJsonURL != null) {
            try {
                final String json = IOUtils.toString(flowBuildInfoJsonURL, StandardCharsets.UTF_8);
                if (flowBuildInfoJsonContainsProductionModeTrue(json)) {
                    log.info("Vaadin production mode is on: " + flowBuildInfoJsonURL + " contains '\"productionMode\": true'");
                    return true;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            log.info("Vaadin production mode is off: " + flowBuildInfoJsonURL + " doesn't contain '\"productionMode\": true'");
            return false;
        }
        log.info("Vaadin production mode is off: META-INF/VAADIN/config/flow-build-info.json is missing");
        return false;
    }

    /**
     * Yeah, we should use JSON parsing, but I'm trying to keep the dependency set minimal.
     */
    @NotNull
    private static final Pattern FLOW_BUILD_INF_JSON_PRODUCTION_MODE_REGEX = Pattern.compile("\"productionMode\":\\s*true");
    @VisibleForTesting
    static boolean flowBuildInfoJsonContainsProductionModeTrue(@NotNull String flowBuildInfoJson) {
        return FLOW_BUILD_INF_JSON_PRODUCTION_MODE_REGEX.matcher(flowBuildInfoJson).find();
    }

    /**
     * Detects whether Vaadin is configured to run in production mode or not.
     */
    public static final boolean isVaadinProductionMode = detectProductionMode();

    /**
     * Returns the JVM major version.
     * @return JVM major version, such as 17 or 21.
     */
    static int getJavaVersion() {
        return Runtime.version().version().get(0);
    }

    /**
     * Returns a short string containing Java version and OS info, for example
     * <code>Java: Amazon.com Inc. 17.0.5, major version 17, OS: amd64 Linux 5.19.0-35-generic</code>
     * @return short host info
     */
    @NotNull
    static String dumpHost() {
        final String os = System.getProperty("os.arch") + " " + System.getProperty("os.name") + " " + System.getProperty("os.version");
        final String java = System.getProperty("java.vendor") + " " + System.getProperty("java.version") + ", major version " + getJavaVersion();
        return "Java: " + java + ", OS: " + os;
    }

    /**
     * Resolves a Vaadin Boot configuration property value: first from {@link System#getenv()}, then from {@link System#getProperties()},
     * then from the default value.
     * @param envVariableName the environment variable name, e.g. <code>SERVER_PORT</code>
     * @param systemPropertyName the Java system property name, passed via command-line as <code>-Dserver.port=8081</code>. Takes
     *                           priority over environment variable name.
     * @param defaultValue the default value, used if none of the above is specified. Not null.
     * @return the value of the configuration property.
     */
    @NotNull
    static String getProperty(@NotNull String envVariableName, @NotNull String systemPropertyName, @NotNull String defaultValue) {
        String result = getProperty(envVariableName, systemPropertyName);
        if (result == null || result.isBlank()) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Resolves a Vaadin Boot configuration property value: first from {@link System#getenv()}, then from {@link System#getProperties()},
     * then from the default value.
     * @param envVariableName the environment variable name, e.g. <code>SERVER_PORT</code>
     * @param systemPropertyName the Java system property name, passed via command-line as <code>-Dserver.port=8081</code>. Takes
     *                           priority over environment variable name.
     * @return the value of the configuration property.
     */
    @Nullable
    static String getProperty(@NotNull String envVariableName, @NotNull String systemPropertyName) {
        Objects.requireNonNull(envVariableName);
        Objects.requireNonNull(systemPropertyName);
        String result = System.getProperty(systemPropertyName);
        if (result == null || result.isBlank()) {
            result = ENV_RESOLVER.apply(envVariableName);
        }
        return result;
    }

    /**
     * Both {@link #getProperty(String, String)} and {@link #getProperty(String, String, String)} honor env
     * variables from this function. Only used for testing.
     */
    @VisibleForTesting
    static Function<String, String> ENV_RESOLVER = System::getenv;
}