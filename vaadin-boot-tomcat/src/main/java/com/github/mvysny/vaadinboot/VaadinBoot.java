package com.github.mvysny.vaadinboot;

import com.vaadin.open.Open;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Bootstraps your Vaadin application from your main() function. Simply call
 * <pre>
 * new VaadinBoot().run();
 * </pre>
 * from your <code>main()</code> method.
 * <br/>
 * By default, listens on all interfaces; call {@link #localhostOnly()} to only
 * listen on localhost.
 */
public class VaadinBoot {
    /**
     * The default port where Tomcat will listen for http:// traffic.
     */
    private static final int DEFAULT_PORT = 8080;
    @NotNull
    private final String mainJarNameRegex;

    /**
     * The port where Jetty will listen for http:// traffic. Defaults to {@value #DEFAULT_PORT}.
     * <br/>
     * Can be configured via the <code>SERVER_PORT</code> environment variable, or <code>-Dserver.port=</code> Java system property.
     */
    @VisibleForTesting
    int port = Integer.parseInt(Env.getProperty("SERVER_PORT", "server.port", "" + DEFAULT_PORT));

    /**
     * Listen on interface handling given host name. Defaults to <code>null</code> which causes Tomcat
     * to listen on all interfaces.
     * <br/>
     * Can be configured via the <code>SERVER_ADDRESS</code> environment variable, or <code>-Dserver.address=</code> Java system property.
     */
    @Nullable
    @VisibleForTesting
    String hostName = Env.getProperty("SERVER_ADDRESS", "server.address", "0.0.0.0");

    /**
     * The context root to run under. Defaults to "".
     * Change this to e.g. /foo to host your app on a different context root
     * <br/>
     * Can be configured via the <code>SERVER_SERVLET_CONTEXT-PATH</code> environment variable, or <code>-Dserver.servlet.context-path=</code> Java system property.
     */
    @NotNull
    @VisibleForTesting
    String contextRoot = Env.getProperty("SERVER_SERVLET_CONTEXT-PATH", "server.servlet.context-path", "");

    /**
     * When the app launches, open the browser automatically when in dev mode.
     */
    private boolean openBrowserInDevMode = true;

    /**
     * Creates new boot instance.
     * @param mainJarNameRegex the regex of the main app jar file name, e.g. <code>testapp-.*\\.jar</code>
     */
    public VaadinBoot(@NotNull @RegExp String mainJarNameRegex) {
        this.mainJarNameRegex = mainJarNameRegex;
    }

    /**
     * Sets the port to listen on. Listens on {@value #DEFAULT_PORT} by default.
     * @param port the new port, 1..65535
     * @return this
     */
    @NotNull
    public VaadinBoot setPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Parameter port: invalid value " + port + ": must be 1..65535");
        }
        this.port = port;
        return this;
    }

    /**
     * Listen on network interface handling given host name. Pass in null to listen on all interfaces;
     * pass in `127.0.0.1` or `localhost` to listen on localhost only (or call {@link #localhostOnly()}).
     * @param hostName the interface to listen on.
     * @return this
     */
    @NotNull
    public VaadinBoot listenOn(@Nullable String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Listen on the <code>localhost</code> network interface only.
     * @return this
     */
    @NotNull
    public VaadinBoot localhostOnly() {
        return listenOn("localhost");
    }

    /**
     * Change this to e.g. /foo to host your app on a different context root
     * @param contextRoot the new context root, e.g. `/foo`.
     * @return this
     */
    @NotNull
    public VaadinBoot withContextRoot(@NotNull String contextRoot) {
        this.contextRoot = Objects.requireNonNull(contextRoot);
        return this;
    }

    /**
     * When the app launches, open the browser automatically when in dev mode.
     * @param openBrowserInDevMode defaults to true.
     * @return this
     */
    @NotNull
    public VaadinBoot openBrowserInDevMode(boolean openBrowserInDevMode) {
        this.openBrowserInDevMode = openBrowserInDevMode;
        return this;
    }

    /**
     * When the app launches, open the browser automatically when in dev mode.
     * @return if true, open the browser automatically when in dev mode.
     */
    public boolean isOpenBrowserInDevMode() {
        return openBrowserInDevMode;
    }

    /**
     * Returns the URL where the app is running, for example <code>http://localhost:8080/app</code>.
     * @return the server URL, not null.
     */
    @NotNull
    public String getServerURL() {
        return "http://" + (hostName != null ? hostName : "localhost") + ":" + port + contextRoot;
    }

    // mark volatile: might be accessed by the shutdown hook from a different thread.
    private volatile Tomcat server;

    /**
     * Runs your app. Blocks until the user presses Enter or CTRL+C.
     * <br/>
     * WARNING: this function may never terminate when the entire JVM may be killed on CTRL+C.
     * <br/>
     * If you wish to wait for the Enter press yourself (if you want to control the lifecycle yourself),
     * call {@link #start()}/{@link #stop(String)} instead of this function.
     * @throws Exception when the webapp fails to start.
     */
    public void run() throws Exception {
        start();

        // We want to shut down the app cleanly by calling stop().
        // Unfortunately, that's not easy. When running from:
        // * Intellij as a Java app: CTRL+C doesn't work but Enter does.
        // * ./gradlew run: Enter doesn't work (no stdin); CTRL+C kills the app forcibly.
        // * ./mvnw exec:java: both CTRL+C and Enter works properly.
        // * cmdline as an unzipped app (production): both CTRL+C and Enter works properly.
        // Therefore, we'll use a combination of the two.

        // this gets called both when CTRL+C is pressed, and when main() terminates.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop("Shutdown hook called, shutting down")));
        System.out.println("Press ENTER or CTRL+C to shutdown");

        if (isOpenBrowserInDevMode() && !Env.isVaadinProductionMode) {
            Open.open(getServerURL());
        }

        // Await for Enter.
        if (System.in.read() == -1) {
            // "./gradlew" run offers no stdin and read() will return immediately with -1
            // This happens when we're running from Gradle; but also when running from Docker with no tty
            System.out.println("No stdin available. press CTRL+C to shutdown");
            server.getServer().await(); // blocks endlessly
        } else {
            // Enter pressed - shut down.
            stop("Main: Shutting down");
        }
    }

    /**
     * Starts the Jetty server and your app. Blocks until the app is fully started, then returns.
     * Mostly used for testing.
     * @throws Exception when the webapp fails to start.
     */
    public void start() throws Exception {
        final long startupMeasurementSince = System.currentTimeMillis();
        log.info("Starting App");

        // detect&enable production mode, but only if it hasn't been specified by the user already
        if (System.getProperty("vaadin.productionMode") == null && Env.isVaadinProductionMode) {
            // fixes https://github.com/mvysny/vaadin14-embedded-jetty/issues/1
            System.setProperty("vaadin.productionMode", "true");
        }

        server = new Tomcat();
        // first thing we need to do is to configure the basedir: if the basedir is configured
        // after connector is created, the setting will be ignored.
        final File basedir = Files.createTempDirectory("tomcat-" + port).toFile().getAbsoluteFile();
        server.setBaseDir(basedir.getAbsolutePath());
        log.debug("Tomcat basedir configured to " + basedir);
        server.setPort(port);
        server.setHostname(hostName);
        server.getConnector(); // make sure the Connector is created so that Tomcat listens for http on 8080
        log.debug("Tomcat Connector created");

        final Context context = createWebAppContext();
        log.debug("Tomcat Context created");

        try {
            server.start();
            log.debug("Tomcat Server started");

            onStarted(context);

            final Duration startupDuration = Duration.ofMillis(System.currentTimeMillis() - startupMeasurementSince);
            System.out.println("\n\n=================================================\n" +
                    "Started in " + startupDuration + ". Running on " + Env.dumpHost() + "\n" +
                    "Please open " + getServerURL() + " in your browser.");
            if (!Env.isVaadinProductionMode) {
                System.out.println("If you see the 'Unable to determine mode of operation' exception, just kill me and run `./gradlew vaadinPrepareFrontend` or `./mvnw vaadin:prepare-frontend`");
            }
            System.out.println("=================================================\n");
        } catch (Exception e) {
            stop("Failed to start");
            throw e;
        }
    }

    /**
     * Invoked when the Tomcat server has been started. By default, does nothing. You can
     * for example dump the quickstart configuration here.
     * @param context the web app context.
     * @throws IOException on i/o exception
     */
    protected void onStarted(@NotNull Context context) throws IOException {
    }

    /**
     * Creates the Tomcat {@link Context}.
     * @return the {@link Context}
     * @throws IOException on i/o exception
     */
    @NotNull
    protected Context createWebAppContext() throws IOException {
        final File webappFolderDev = new File("src/dist/webapp").getAbsoluteFile();
        final File webappFolderProd = new File("../webapp").getAbsoluteFile();
        File docBase = webappFolderDev;
        if (!docBase.exists()) {
            docBase = webappFolderProd;
        }
        if (!docBase.exists()) {
            throw new IllegalStateException("Invalid state: The webapp folder isn't present neither at " + webappFolderDev + " (development mode) nor at " + webappFolderProd + " (production)");
        }

        final Context ctx = server.addWebapp(contextRoot, docBase.getAbsolutePath());
        // we need to add classes to Tomcat to enable classpath scanning, in order to
        // auto-discover app @WebServlet and @WebListener.
        final File classDirMaven = new File("target/classes").getAbsoluteFile();
        final File classDirGradle = new File("build/classes").getAbsoluteFile();
        File additionWebInfClasses = classDirMaven;  // dev env with Maven
        if (!additionWebInfClasses.exists()) {
            additionWebInfClasses = classDirGradle;  // dev env with Gradle
        }
        if (additionWebInfClasses.exists()) {
            final WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        } else {
            final File libs = new File("../lib").getAbsoluteFile();
            if (!libs.exists()) {
                throw new IllegalStateException("Invalid state: " + libs + " does not exist");
            }
            final File[] possibleProductionJarFilesArray = libs.listFiles((dir, name) -> name.matches(mainJarNameRegex));
            final List<File> possibleProductionJarFiles = possibleProductionJarFilesArray == null ? Collections.emptyList() : Arrays.asList(possibleProductionJarFilesArray);
            if (possibleProductionJarFiles.size() != 1) {
                throw new IllegalStateException("Invalid state: expected exactly one app jar file " + mainJarNameRegex + " but got " + possibleProductionJarFiles);
            }
            final File productionJar = possibleProductionJarFiles.get(0);
            if (!productionJar.exists()) {
                throw new IllegalStateException("Invalid state: " + productionJar + " doesn't exist");
            }
            final WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new JarResourceSet(resources, "/WEB-INF/classes",
                    productionJar.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }
        return ctx;
    }

    /**
     * Stops your app. Blocks until the webapp is fully stopped. Mostly used for tests.
     * Never throws an exception.
     * @param reason why we're shutting down. Logged as info.
     */
    public void stop(@NotNull String reason) {
        try {
            if (server != null) {
                log.info(reason);
                server.stop(); // blocks until the webapp stops fully
                log.info("Stopped");
                server = null;
            }
        } catch (Throwable t) {
            log.error("stop() failed: " + t, t);
        }
    }

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(VaadinBoot.class);
}
