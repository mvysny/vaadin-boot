package com.github.mvysny.vaadinboot;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Bootstraps your Vaadin application from your main() function. Simply call
 * <code><pre>
 * new VaadinBoot().withArgs(args).run();
 * </pre></code>
 * from your main() method.
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

    public VaadinBoot() {
        try {
            servlet = Class.forName("com.vaadin.flow.server.VaadinServlet").asSubclass(Servlet.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public VaadinBoot setPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Parameter port: invalid value " + port + ": must be 1..65535");
        }
        this.port = port;
        return this;
    }

    /**
     * Listen on interfaces handling given host name. Pass in null to listen on all interfaces;
     * pass in `127.0.0.1` or `localhost` to listen on localhost only.
     * @param hostName the interface to listen on.
     * @return this
     */
    @NotNull
    public VaadinBoot listenOn(@Nullable String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Listen on localhost only.
     * @return this
     */
    @NotNull
    public VaadinBoot localhostOnly() {
        return listenOn("localhost");
    }

    @NotNull
    public VaadinBoot withArgs(@NotNull String[] args) {
        if (args.length >= 1) {
            setPort(Integer.parseInt(args[0]));
        }
        return this;
    }

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

    // mark volatile: might be accessed by the shutdown hook from a different thread.
    private volatile Server server;

    /**
     * Runs your app. Blocks until the user presses Enter or CTRL+C.
     * <p></p>
     * WARNING: JVM may be killed on CTRL+C; don't place any Java code after this function has been called from your main().
     * @throws Exception
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
        // Await for Enter.  ./gradlew run offers no stdin and read() will return immediately with -1
        if (System.in.read() == -1) {
            // running from Gradle
            System.out.println("Running from Gradle, press CTRL+C to shutdown");
            server.join(); // blocks endlessly
        } else {
            stop("Main: Shutting down");
        }
    }

    /**
     * Starts the Jetty server and your app. Blocks until the app is fully started, then
     * resumes execution. Mostly used for testing.
     * @throws Exception
     */
    public void start() throws Exception {
        // detect&enable production mode
        if (isProductionMode()) {
            // fixes https://github.com/mvysny/vaadin14-embedded-jetty/issues/1
            System.out.println("Production mode detected, enforcing");
            System.setProperty("vaadin.productionMode", "true");
        }

        final WebAppContext context = new WebAppContext();
        context.setBaseResource(findWebRoot());
        context.setContextPath(contextRoot);
        context.addServlet(servlet, "/*");
        // this will properly scan the classpath for all @WebListeners, including the most important
        // com.vaadin.flow.server.startup.ServletContextListeners.
        // See also https://mvysny.github.io/vaadin-lookup-vs-instantiator/
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*\\.jar|.*/classes/.*");
        context.setConfigurationDiscovered(true);
        context.getServletContext().setExtendedListenerTypes(true);

        if (hostName != null) {
            server = new Server(new InetSocketAddress(hostName, port));
        } else {
            server = new Server(port);
        }
        server.setHandler(context);
        server.start();

        System.out.println("\n\n=================================================\n" +
                "Please open http://localhost:" + port + contextRoot + " in your browser\n" +
                "If you see the 'Unable to determine mode of operation' exception, just kill me and run `./gradlew vaadinPrepareFrontend`\n" +
                "=================================================\n");
    }

    /**
     * Stops your app. Blocks until the webapp is fully stopped. Mostly used for tests.
     * @param reason
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

    private static boolean isProductionMode() {
        final String probe = "META-INF/maven/com.vaadin/flow-server-production-mode/pom.xml";
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource(probe) != null;
    }

    @NotNull
    private static Resource findWebRoot() throws MalformedURLException {
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
        URL webRoot = new URL(url.substring(0, url.length() - 5));
        log.info("WebRoot is " + webRoot);
        return Resource.newResource(webRoot);
    }

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(VaadinBoot.class);
}
