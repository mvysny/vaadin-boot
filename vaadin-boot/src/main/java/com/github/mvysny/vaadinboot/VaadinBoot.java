package com.github.mvysny.vaadinboot;

import com.vaadin.open.Open;
import jakarta.servlet.Servlet;
import org.eclipse.jetty.quickstart.QuickStartConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
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
     * Parses given command-line parameters. At the moment only the port number is
     * parsed out if the array is non-empty.
     * @param args the command-line parameters, not null.
     * @return this
     */
    @NotNull
    public VaadinBoot withArgs(@NotNull String[] args) {
        if (args.length >= 1) {
            setPort(Integer.parseInt(args[0]));
        }
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

    // mark volatile: might be accessed by the shutdown hook from a different thread.
    private volatile Server server;

    /**
     * Runs your app. Blocks until the user presses Enter or CTRL+C.
     * <p></p>
     * WARNING: this function may never terminate since the entire JVM may be killed on CTRL+C.
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
     */
    public void start() throws Exception {
        final long startupMeasurementSince = System.currentTimeMillis();
        log.info("Starting App");

        // detect&enable production mode
        if (Env.isVaadinProductionMode) {
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
        server.start();
        log.debug("Jetty Server started");

        if (createQuickStartXml) {
            JettyQuickStart.generateQuickStartXml(context);
        }

        final Duration startupDuration = Duration.ofMillis(System.currentTimeMillis() - startupMeasurementSince);
        System.out.println("\n\n=================================================\n" +
                "Started in " + startupDuration + ". Running on " + Env.dumpHost() + "\n" +
                "Please open " + getServerURL() + " in your browser.");
        if (!Env.isVaadinProductionMode) {
            System.out.println("If you see the 'Unable to determine mode of operation' exception, just kill me and run `./gradlew vaadinPrepareFrontend` or `./mvnw vaadin:prepare-frontend`");
        }
        System.out.println("=================================================\n");
    }

    /**
     * Creates the Jetty {@link WebAppContext}.
     * @return the {@link WebAppContext}
     */
    @NotNull
    protected WebAppContext createWebAppContext() throws MalformedURLException {
        final WebAppContext context = new WebAppContext();
        final Resource webRoot = Env.findWebRoot();
        context.setBaseResource(webRoot);
        context.setContextPath(contextRoot);
        context.addServlet(servlet, "/*");
        if (JettyQuickStart.quickstartXmlExists(webRoot)) {
            context.setAttribute(QuickStartConfiguration.MODE, QuickStartConfiguration.Mode.QUICKSTART);
            context.addConfiguration(new QuickStartConfiguration());
        } else {
            // this will properly scan the classpath for all @WebListeners, including the most important
            // com.vaadin.flow.server.startup.ServletContextListeners.
            // See also https://mvysny.github.io/vaadin-lookup-vs-instantiator/
            // Jetty documentation: https://www.eclipse.org/jetty/documentation/jetty-12/operations-guide/index.html#og-annotations-scanning
            context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*\\.jar|.*/classes/.*");
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

    @NotNull
    private QuickStartMode quickStartMode = QuickStartMode.Off;

    /**
     * Jetty can optionally start faster if we don't classpath-scan for resources,
     * and instead pass in a QuickStart XML file with all resources listed.
     * <p></p>
     * This is mandatory for native mode.
     * <p></p>
     * See
     * <a href="https://www.eclipse.org/jetty/documentation/jetty-12/operations-guide/index.html#og-quickstart">Jetty QuickStart documentation</a>
     * for more details; see
     * <a href="https://www.eclipse.org/jetty/documentation/jetty-12/programming-guide/index.html#jetty-effective-web-xml-goal">Jetty Maven plugin</a>
     * documentation as well. Also see <a href="https://github.com/mvysny/vaadin-boot/issues/11">Issue #11</a>.
     * @param quickStartMode the new quick start mode, defaults to {@link QuickStartMode#Off}.
     * @return this
     */
    @NotNull
    public VaadinBoot withQuickStartMode(@NotNull QuickStartMode quickStartMode) {
        this.quickStartMode = quickStartMode;
        return this;
    }

    private boolean createQuickStartXml = false;

    /**
     * Defaults to false. If true, a <code>quickstart-web.xml</code> file for your app is created in the
     * current working directory when Jetty starts.
     * <p></p>
     * Workaround until we are able to generate the XML file during the compile time, via a Maven/Gradle plugin.
     * @return this
     */
    @NotNull
    public VaadinBoot generateQuickStartXml() {
        createQuickStartXml = true;
        return this;
    }

    public enum QuickStartMode {
        /**
         * Never use Jetty Quick Start - always use classpath scanning.
         */
        Off {
            @Override
            public boolean isQuickstartEnabled() {
                return false;
            }
        },
        /**
         * Use Jetty Quick Start only when running in Vaadin production mode.
         */
        Production {
            @Override
            public boolean isQuickstartEnabled() {
                return Env.isVaadinProductionMode;
            }
        },
        /**
         * Use Jetty Quick Start, both in dev and in production mode.
         */
        Always {
            @Override
            public boolean isQuickstartEnabled() {
                return true;
            }
        };
        public abstract boolean isQuickstartEnabled();
    }
}
