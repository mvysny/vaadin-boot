package com.github.mvysny.vaadinboot;

import com.vaadin.open.Open;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;

/**
 * Bootstraps your Vaadin application from your main() function. Simply call
 * <code><pre>
 * new VaadinBoot().withArgs(args).run();
 * </pre></code>
 * from your <code>main()</code> method.
 * <p></p>
 * By default, listens on all interfaces; call {@link #localhostOnly()} to only
 * listen on localhost.
 */
public class VaadinBoot {
    /**
     * The default port where Jetty will listen for http:// traffic.
     */
    private static final int DEFAULT_PORT = 8080;

    /**
     * The port where Jetty will listen for http:// traffic.
     */
    @VisibleForTesting
    int port = DEFAULT_PORT;

    /**
     * The VaadinServlet.
     */
    @VisibleForTesting
    @NotNull
    Class<? extends Servlet> servlet;

    /**
     * Listen on interface handling given host name. Defaults to null which causes Jetty
     * to listen on all interfaces.
     */
    @Nullable
    private String hostName = null;

    /**
     * The context root to run under. Defaults to `/`.
     * Change this to e.g. /foo to host your app on a different context root
     */
    @NotNull
    private String contextRoot = "/";

    /**
     * When the app launches, open the browser automatically when in dev mode.
     */
    private boolean openBrowserInDevMode = true;

    /**
     * If true, no classpath scanning is performed - no servlets nor weblisteners are detected.
     * <p></p>
     * This will most probably cause Vaadin to not work and throw NullPointerException at <code>VaadinServlet.serveStaticOrWebJarRequest</code>.
     * However, it's a good thing to disable this when starting your app with a QuickStart configuration.
     */
    private boolean disableClasspathScanning = false;

    /**
     * If true, the test classpath will also be scanned for annotations. Defaults to false.
     * <p></p>
     * Only set to true if you have Vaadin routes in <code>src/test/java/</code> - it's
     * a bit of an antipattern but quite common with Vaadin addons. See
     * <a href="https://github.com/mvysny/vaadin-boot/issues/15">Issue #15</a> for more details.
     * <p></p>
     * Ignored if {@link #disableClasspathScanning} is true.
     */
    private boolean isScanTestClasspath = false;

    /**
     * Creates the new instance of the Boot launcher.
     */
    public VaadinBoot() {
        try {
            servlet = Class.forName("com.vaadin.flow.server.VaadinServlet").asSubclass(Servlet.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
     * @deprecated does nothing. See <a href="https://github.com/mvysny/vaadin-boot/issues/12">#12</a> for more details.
     */
    @Deprecated
    @NotNull
    public VaadinBoot withArgs(@NotNull String[] args) {
        return this;
    }

    /**
     * Bootstraps custom servlet instead of the default <code>com.vaadin.flow.server.VaadinServlet</code>.
     * @param vaadinServlet the custom servlet, not null.
     * @return this
     */
    @NotNull
    public VaadinBoot withServlet(@NotNull Class<? extends Servlet> vaadinServlet) {
        this.servlet = Objects.requireNonNull(vaadinServlet);
        return this;
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
     * Returns the URL where the app is running, for example <code>http://localhost:8080/app</code>.
     * @return the server URL, not null.
     */
    @NotNull
    public String getServerURL() {
        return "http://" + (hostName != null ? hostName : "localhost") + ":" + port + contextRoot;
    }

    /**
     * If true, no classpath scanning is performed - no servlets nor weblisteners are detected.
     * <p></p>
     * This will most probably cause Vaadin to not work and throw NullPointerException at <code>VaadinServlet.serveStaticOrWebJarRequest</code>.
     * However, it's a good thing to disable this when starting your app with a QuickStart configuration.
     */
    @NotNull
    public VaadinBoot disableClasspathScanning() {
        disableClasspathScanning = true;
        return this;
    }

    /**
     * When called, the test classpath will also be scanned for annotations. Defaults to false.
     * <p></p>
     * Use only in case when you have Vaadin routes in <code>src/test/java/</code> - it's
     * a bit of an antipattern but quite common with Vaadin addons. See
     * <a href="https://github.com/mvysny/vaadin-boot/issues/15">Issue #15</a> for more details.
     * <p></p>
     * Ignored if {@link #disableClasspathScanning} is true.
     */
    @NotNull
    public VaadinBoot scanTestClasspath() {
        isScanTestClasspath = true;
        return this;
    }

    // mark volatile: might be accessed by the shutdown hook from a different thread.
    private volatile Server server;

    /**
     * Runs your app. Blocks until the user presses Enter or CTRL+C.
     * <p></p>
     * WARNING: this function may never terminate when the entire JVM may be killed on CTRL+C.
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

        if (openBrowserInDevMode && !Env.isVaadinProductionMode) {
            Open.open(getServerURL());
        }

        // Await for Enter.
        if (System.in.read() == -1) {
            // "./gradlew" run offers no stdin and read() will return immediately with -1
            // This happens when we're running from Gradle; but also when running from Docker with no tty
            System.out.println("No stdin available. press CTRL+C to shutdown");
            server.join(); // blocks endlessly
        } else {
            stop("Main: Shutting down");
        }
    }

    /**
     * Starts the Jetty server and your app. Blocks until the app is fully started, then
     * resumes execution. Mostly used for testing.
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

        fixClasspath();
        log.debug("Classpath fixed");

        final WebAppContext context = createWebAppContext();
        log.debug("Jetty WebAppContext created");

        if (hostName != null) {
            server = new Server(new InetSocketAddress(hostName, port));
        } else {
            server = new Server(port);
        }
        server.setHandler(context);
        log.debug("Jetty Server configured");
        try {
            server.start();
            log.debug("Jetty Server started");

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
     * Invoked when the Jetty server has been started. By default, does nothing. You can
     * for example dump the quickstart configuration here.
     * @param context the web app context.
     */
    protected void onStarted(@NotNull WebAppContext context) throws IOException {
    }

    /**
     * Creates the Jetty {@link WebAppContext}.
     * @return the {@link WebAppContext}
     */
    @NotNull
    protected WebAppContext createWebAppContext() throws IOException {
        final WebAppContext context = new WebAppContext();
        context.setBaseResource(Env.findWebRoot());
        context.setContextPath(contextRoot);
        context.addServlet(servlet, "/*");
        // when the webapp fails to initialize, make sure that start() throws.
        context.setThrowUnavailableOnStartupException(true);
        if (!disableClasspathScanning) {
            // this will properly scan the classpath for all @WebListeners, including the most important
            // com.vaadin.flow.server.startup.ServletContextListeners.
            // See also https://mvysny.github.io/vaadin-lookup-vs-instantiator/
            // Jetty documentation: https://www.eclipse.org/jetty/documentation/jetty-12/operations-guide/index.html#og-annotations-scanning
            String pattern = ".*\\.jar|.*/classes/.*";
            if (isScanTestClasspath) {
                pattern += "|.*/test-classes/.*";
            }
            context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", pattern);
            context.setConfigurationDiscovered(true);
        }
        return context;
    }

    /**
     * See {@link Env#fixClasspath()}.
     */
    protected void fixClasspath() {
        Env.fixClasspath();
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
