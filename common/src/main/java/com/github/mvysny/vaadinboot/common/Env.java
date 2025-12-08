package com.github.mvysny.vaadinboot.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Environment-related utility functions. Internal to Vaadin-Boot, don't use - the API can change at any time.
 */
public final class Env {
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
    private static final Pattern FLOW_BUILD_INF_JSON_PRODUCTION_MODE_REGEX = Pattern.compile("\"productionMode\"\\s*:\\s*true");
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
    public static int getJavaVersion() {
        return Runtime.version().version().get(0);
    }

    /**
     * Returns a short string containing Java version and OS info, for example
     * <code>Java: Amazon.com Inc. 17.0.5, major version 17, OS: amd64 Linux 5.19.0-35-generic</code>
     * @return short host info
     */
    @NotNull
    public static String dumpHost() {
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
    public static String getProperty(@NotNull String envVariableName, @NotNull String systemPropertyName, @NotNull String defaultValue) {
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
    public static String getProperty(@NotNull String envVariableName, @NotNull String systemPropertyName) {
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

    /**
     * True if it looks like we're running in a development environment, false if we're most probably running from a production zip file.
     * Note that this may differ from {@link #isVaadinProductionMode}: it's possible to run vaadin dev mode from a zip file for example,
     * even though it makes little sense. Also, it's possibly to develop Vaadin app while having Vaadin running in production mode -
     * again highly unusual but might make sense in certain dev envs.
     */
    public static final boolean isDevelopmentEnvironment;
    static {
        // check whether there's a pom.xml or build.gradle(.kts) - if yes, we're running in development environment (most probably
        // from an IDE launch configuration).
        isDevelopmentEnvironment =
                new File("pom.xml").exists() ||
                        new File("build.gradle").exists() ||
                        new File("build.gradle.kts").exists();
    }

    /**
     * Detects the <code>/webapp</code> web root folder, used to serve static content.
     * @return resource serving web root.
     * @throws MalformedURLException when the webroot URL auto-detection fails and produces an invalid URL.
     */
    @NotNull
    public static URL findWebRoot() throws MalformedURLException {
        // don't look up directory as a resource, it's unreliable: https://github.com/eclipse/jetty.project/issues/4173#issuecomment-539769734
        // instead we'll look up the /webapp/ROOT and retrieve the parent folder from that.
        final URL f = Env.class.getResource("/webapp/ROOT");
        if (f == null) {
            throw new IllegalStateException("Invalid state: the resource /webapp/ROOT doesn't exist, has the 'webapp' folder been packaged in as a resource?");
        }
        final String url = f.toExternalForm();
        if (!url.endsWith("/ROOT")) {
            throw new RuntimeException("Parameter url: invalid value " + url + ": doesn't end with /ROOT");
        }

        // Resolve file to directory
        final URL webRoot;
        try {
            webRoot = new URI(url.substring(0, url.length() - 5)).toURL();
        } catch (URISyntaxException e) {
            final MalformedURLException ex = new MalformedURLException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        log.info("WebRoot is served from " + webRoot);
        return webRoot;
    }

    /**
     * The `webapp` folder is being served from a resources folder, or from a jar file.
     * @param webRoot the `webapp` folder resource as detected by {@link #findWebRoot()}.
     * @return the jar file or a directory from which the class files are being served.
     * @throws IOException on i/o error
     */
    @NotNull
    public static File findResourcesJarOrFolder(@NotNull URL webRoot) throws IOException {
        // we know that the `webapp` folder is somewhere on classpath, and webRoot parameter is pointing to it.
        // we need to figure out where exactly on the filesystem the folder is.
        final File file = FileUtils.toFile(webRoot);
        if (file != null) {
            // serving the `webapp` folder from a directory
            final File classDirectory = file.getAbsoluteFile().getParentFile();
            Objects.requireNonNull(classDirectory, () -> "Unexpected: " + file.getAbsoluteFile() + " has no parent!");
            if (!classDirectory.exists()) {
                throw new IllegalStateException("Invalid state: " + classDirectory + " doesn't exist");
            }
            if (!classDirectory.isDirectory()) {
                // this should never happen since File.getParentFile() should always return a directory!
                throw new IllegalStateException("Invalid state: " + classDirectory + " is not a directory");
            }
            return classDirectory;
        }
        // the webapp folder is served from a jar file. Find the name of the jar file.
        // example URL:
        //  jar:file:/mnt/disk1/mavi/work/my/vaadin-boot/testapp-tomcat/build/distributions/testapp-tomcat-12.4-SNAPSHOT/lib/testapp-tomcat-12.4-SNAPSHOT.jar!/webapp
        if (!"jar".equals(webRoot.getProtocol())) {
            throw new IllegalArgumentException("Parameter webRoot: invalid value " + webRoot + ": unsupported URL type");
        }
        // path looks like: file:/mnt/disk1/mavi/work/my/vaadin-boot/testapp-tomcat/build/distributions/testapp-tomcat-12.4-SNAPSHOT/lib/testapp-tomcat-12.4-SNAPSHOT.jar!/webapp
        final String path = webRoot.getPath();
        if (!path.endsWith("!/webapp")) {
            throw new IllegalStateException("Invalid state: unexpected path " + path);
        }
        final URL url = URI.create(path.substring(0, path.length() - 8)).toURL();
        final File jarFile = FileUtils.toFile(url);
        if (jarFile == null) {
            throw new IllegalStateException("Invalid state: can't convert URL to file: " + url);
        }
        if (!jarFile.exists()) {
            throw new IllegalStateException("Invalid state: doesn't exist: " + jarFile);
        }
        if (!jarFile.isFile()) {
            throw new IllegalStateException("Invalid state: not a file: " + jarFile);
        }
        return jarFile;
    }

    /**
     * Returns a set of folders or jar files with app's classes. Only this project's
     * modules and submodules are considered - third-party dependencies are never present.
     * @param webRoot produced by {@link #findWebRoot()}.
     * @return a set of folders, or a set of a single jar file. Never empty, never contains non-existing files.
     * @throws IOException if the detection fails.
     */
    @NotNull
    public static Set<File> findClassesJarOrFolder(@NotNull URL webRoot) throws IOException {
        if (!isDevelopmentEnvironment) {
            // when running from a zip file, we expect classes and resources
            // to be packaged into the same jar.
            return Set.of(findResourcesJarOrFolder(webRoot));
        }

        // Approach 1: analyze the classpath
        final String classpath = System.getProperty("java.class.path");
        if (classpath != null) {
            final String[] entries = classpath.split("[" + File.pathSeparator + "]");
            final Set<File> foldersOnClasspath = Arrays.stream(entries)
                    .filter(it -> !it.isBlank())
                    .map(File::new)
                    .filter(it -> it.exists() && it.isDirectory() && it.getAbsolutePath().contains("/classes"))
                    .collect(Collectors.toSet());
            if (!foldersOnClasspath.isEmpty()) {
                return foldersOnClasspath;
            }
        }

        // Approach 2: maybe the classloader is the URLClassLoader?
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader instanceof URLClassLoader) {
            final URL[] urls = ((URLClassLoader) contextClassLoader).getURLs();
            final Set<File> foldersOnClasspath = Arrays.stream(urls)
                    .map(FileUtils::toFile)
                    .filter(it -> it != null && it.exists() && it.isDirectory() && it.getAbsolutePath().contains("/classes"))
                    .collect(Collectors.toSet());
            if (!foldersOnClasspath.isEmpty()) {
                return foldersOnClasspath;
            }
        }

        // Fallback: just open target/classes or build/classes. Note this depends on the CWD (current working directory)
        // and is known to break when running a submodule in IDEA since IDEA (for some crazy reason)
        // insists on setting CWD to the main project, instead to the submodule.
        final File classDirMaven = new File("target/classes").getAbsoluteFile();
        final File classDirGradle = new File("build/classes").getAbsoluteFile();
        File additionWebInfClasses = classDirMaven;  // dev env with Maven
        if (!additionWebInfClasses.exists()) {
            additionWebInfClasses = classDirGradle;  // dev env with Gradle
        }
        if (!additionWebInfClasses.exists()) {
            throw new IllegalStateException("Invalid state: " + additionWebInfClasses + " does not exist");
        }
        return Set.of(additionWebInfClasses);
    }
}
