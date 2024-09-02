package com.github.mvysny.vaadinboot.common;

import com.vaadin.open.Open;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * Bootstraps your Vaadin application from your <code>main()</code> function. Simply call
 * <pre>
 * new VaadinBoot().run();
 * </pre>
 * from your <code>main()</code> method.
 * <br/>
 * By default, listens on all interfaces; call {@link #localhostOnly()} to only
 * listen on localhost.
 * @param <THIS> the actual <code>VaadinBoot</code> class.
 */
public abstract class VaadinBootBase<THIS extends VaadinBootBase<THIS>> {
    /**
     * The default port where the web server will listen for http:// traffic.
     */
    private static final int DEFAULT_PORT = 8080;

    /**
     * The port where the web server will listen for http:// traffic. Defaults to {@value #DEFAULT_PORT}.
     * <br/>
     * Can be configured via the <code>SERVER_PORT</code> environment variable, or <code>-Dserver.port=</code> Java system property.
     */
    @VisibleForTesting
    int port = Integer.parseInt(Env.getProperty("SERVER_PORT", "server.port", "" + DEFAULT_PORT));

    /**
     * Listen on interface handling given host name. Defaults to <code>null</code> which causes the web server
     * to listen on all interfaces.
     * <br/>
     * Can be configured via the <code>SERVER_ADDRESS</code> environment variable, or <code>-Dserver.address=</code> Java system property.
     */
    @Nullable
    @VisibleForTesting
    String hostName = Env.getProperty("SERVER_ADDRESS", "server.address");

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
     * Creates new Vaadin Boot instance.
     * @param webServer the underlying web server.
     */
    protected VaadinBootBase(@NotNull WebServer webServer) {
        this.server = Objects.requireNonNull(webServer);
    }

    /**
     * Sets the port to listen on. Listens on {@value #DEFAULT_PORT} by default.
     * @param port the new port, 1..65535
     * @return this
     */
    @NotNull
    public THIS setPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Parameter port: invalid value " + port + ": must be 1..65535");
        }
        this.port = port;
        return getThis();
    }

    @NotNull
    private THIS getThis() {
        //noinspection unchecked
        return (THIS) this;
    }

    /**
     * Listen on network interface handling given host name. Pass in <code>null</code> to listen on all interfaces;
     * pass in `127.0.0.1` or `localhost` to listen on localhost only (or call {@link #localhostOnly()}).
     * @param hostName the interface to listen on.
     * @return this
     */
    @NotNull
    public THIS listenOn(@Nullable String hostName) {
        this.hostName = hostName;
        return getThis();
    }

    /**
     * Listen on the <code>localhost</code> network interface only.
     * @return this
     */
    @NotNull
    public THIS localhostOnly() {
        return listenOn("localhost");
    }

    /**
     * Change this to e.g. /foo to host your app on a different context root
     * @param contextRoot the new context root, e.g. `/foo`. Pass in either an empty string or "/" to serve on the base context root.
     * @return this
     */
    @NotNull
    public THIS withContextRoot(@NotNull String contextRoot) {
        this.contextRoot = Objects.requireNonNull(contextRoot);
        return getThis();
    }

    /**
     * When the app launches, open the browser automatically when in dev mode.
     * @param openBrowserInDevMode defaults to true.
     * @return this
     */
    @NotNull
    public THIS openBrowserInDevMode(boolean openBrowserInDevMode) {
        this.openBrowserInDevMode = openBrowserInDevMode;
        return getThis();
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

    @NotNull
    private final WebServer server;

    private volatile boolean serverStopped = false;

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
            server.await(); // blocks endlessly
        } else {
            // Enter pressed - shut down.
            stop("Main: Shutting down");
        }
    }

    private boolean serverStarted = false;

    /**
     * Starts the web server and your app. Blocks until the app is fully started, then returns.
     * Mostly used for testing.
     * @throws Exception when the webapp fails to start.
     */
    public synchronized void start() throws Exception {
        if (serverStarted) {
            throw new IllegalStateException("Invalid state: already has been started - can not be started again");
        }
        final long startupMeasurementSince = System.currentTimeMillis();
        log.info("Starting App");

        // detect&enable production mode, but only if it hasn't been specified by the user already
        if (System.getProperty("vaadin.productionMode") == null && Env.isVaadinProductionMode) {
            // fixes https://github.com/mvysny/vaadin14-embedded-jetty/issues/1
            System.setProperty("vaadin.productionMode", "true");
        }

        server.configure(this);

        try {
            server.start();
            log.debug(server.getName() + " Server started");

            serverStarted = true;
            onStarted(server);

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
     * Invoked when the Web server has been started. By default, does nothing.
     * You can for example dump Jetty's quickstart configuration here.
     * @param server the web server; obtain the context from the concrete web server object.
     * @throws IOException on i/o exception
     */
    protected void onStarted(@NotNull WebServer server) throws IOException {
    }

    /**
     * Stops your app. Blocks until the webapp is fully stopped. Mostly used for tests.
     * Never throws an exception. Does nothing if the web server is already stopped.
     * @param reason why we're shutting down. Logged as info.
     * @throws IllegalStateException if {@link #start()} wasn't called yet.
     */
    public synchronized void stop(@NotNull String reason) {
        if (!serverStarted) {
            throw new IllegalStateException("Invalid state: start() not called yet");
        }
        try {
            if (!serverStopped) {
                log.info(reason);
                server.stop(); // blocks until the webapp stops fully
                log.info("Stopped");
            }
        } catch (Throwable t) {
            log.error("stop() failed: " + t, t);
        }
        serverStopped = true;
    }

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(VaadinBootBase.class);
}
