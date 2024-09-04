package com.github.mvysny.vaadinboot.common;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.eclipse.jetty.ee10.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Wraps an embedded Jetty web server:
 * <ul>
 *     <li>Homepage: <a href="https://jetty.org">jetty.org</a></li>
 *     <li><a href="https://jetty.org/docs/jetty/12/index.html">Documentation for Jetty 12</a></li>
 *     <li>Documentation for Embedded Jetty: <a href="https://jetty.org/docs/jetty/12/programming-guide/server/http.html">HTTP Server Libraries</a></li>
 * </ul>
 */
public class JettyWebServer implements WebServer {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(JettyWebServer.class);

    // mark volatile: might be accessed by the shutdown hook from a different thread.
    private volatile Server server;

    /**
     * Creates a thread pool for Jetty to serve http requests.
     * @return the thread pool, may be null if the default one is to be used.
     */
    @Nullable
    protected ThreadPool newThreadPool(boolean useVirtualThreadsIfAvailable) {
        if (useVirtualThreadsIfAvailable && Env.getJavaVersion() >= 21) {
            log.info("Configuring Jetty to use JVM 21+ virtual threads");
            // see https://eclipse.dev/jetty/documentation/jetty-12/programming-guide/index.html#pg-arch-threads-thread-pool-virtual-threads
            final QueuedThreadPool threadPool = new QueuedThreadPool();
            try {
                // reflection: we call Java 21+ method, however we're compiled with Java 17
                final Method m = Executors.class.getDeclaredMethod("newVirtualThreadPerTaskExecutor");
                threadPool.setVirtualThreadsExecutor(((Executor) m.invoke(null)));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return threadPool;
        } else {
            log.info("Configuring Jetty to use regular JVM threads");
            return null;
        }
    }

    private volatile WebAppContext context;

    @Override
    public void configure(@NotNull VaadinBootBase<?> configuration) throws Exception {
        final VaadinBoot cfg = (VaadinBoot) configuration;

        fixClasspath();
        log.debug("Classpath fixed");

        context = createWebAppContext(cfg);
        log.debug("Jetty WebAppContext created");

        server = new Server(newThreadPool(cfg.isUseVirtualThreadsIfAvailable()));
        final ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(configuration.port);
        if (configuration.hostName != null) {
            serverConnector.setHost(configuration.hostName);
        }
        server.addConnector(serverConnector);
        server.setHandler(context);
        log.debug("Jetty Server configured");
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }

    @Override
    public void await() throws InterruptedException {
        server.join();
    }

    @Override
    public @NotNull String getName() {
        return "Jetty";
    }

    public @NotNull WebAppContext getContext() {
        return Objects.requireNonNull(context);
    }

    /**
     * Creates the Jetty {@link WebAppContext}.
     * @return the {@link WebAppContext}
     * @throws IOException on i/o exception
     */
    @NotNull
    protected WebAppContext createWebAppContext(@NotNull VaadinBoot cfg) throws IOException {
        final WebAppContext context = new WebAppContext();
        final Resource webRoot = findWebRoot(context.getResourceFactory());
        context.setBaseResource(webRoot);
        final String contextRoot = ((VaadinBootBase<?>) cfg).contextRoot;
        context.setContextPath(contextRoot.isEmpty() ? "/" : contextRoot);

        // don't add the servlet this way - the @WebServlet annotation is ignored!
        // https://github.com/mvysny/vaadin-boot/issues/22
//        context.addServlet(servlet, "/*");

        // when the webapp fails to initialize, make sure that start() throws.
        context.setThrowUnavailableOnStartupException(true);
        if (!cfg.isDisableClasspathScanning()) {
            // this will properly scan the classpath for all @WebListeners, including the most important
            // com.vaadin.flow.server.startup.ServletContextListeners.
            // See also https://mvysny.github.io/vaadin-lookup-vs-instantiator/
            // Jetty documentation: https://www.eclipse.org/jetty/documentation/jetty-12/operations-guide/index.html#og-annotations-scanning
            String pattern = ".*\\.jar|.*/classes/.*";
            if (cfg.isScanTestClasspath()) {
                pattern += "|.*/test-classes/.*";
            }
            context.setAttribute(MetaInfConfiguration.CONTAINER_JAR_PATTERN, pattern);
            // must be set to true, to enable classpath scanning:
            // https://eclipse.dev/jetty/documentation/jetty-12/operations-guide/index.html#og-annotations-scanning
            context.setConfigurationDiscovered(true);
        }
        return context;
    }

    /**
     * Detects the web root folder, used to serve static content.
     * @return resource serving web root.
     * @throws MalformedURLException when the webroot URL auto-detection fails and produces an invalid URL.
     */
    @NotNull
    static Resource findWebRoot(ResourceFactory resourceFactory) throws MalformedURLException {
        final URL webRoot = Env.findWebRoot();
        final Resource resource = resourceFactory.newResource(webRoot);
        if (!resource.exists()) {
            log.warn(resource + " (" + resource.getClass().getName() + ") claims it doesn't exist");
        }
        if (!resource.isDirectory()) {
            log.warn(resource + " (" + resource.getClass().getName() + ") is not a directory, Jetty QuickStart will most probably fail");
        }
        return resource;
    }

    /**
     * Removes invalid entries from classpath (stored in system property <code>java.class.path</code>).
     * Fixes Jetty throwing exceptions for non-existing classpath entries.
     * See <a href="https://github.com/mvysny/vaadin-boot/issues/1">Issue #1</a> for more details.
     */
    static void fixClasspath() {
        final String classpath = System.getProperty("java.class.path");
        if (classpath != null) {
            final String[] entries = classpath.split("[" + File.pathSeparator + "]");
            final String filteredClasspath = Arrays.stream(entries)
                    .filter(it -> !it.isBlank() && new File(it).exists())
                    .collect(Collectors.joining(File.pathSeparator));
            System.setProperty("java.class.path", filteredClasspath);
        }
    }
}
