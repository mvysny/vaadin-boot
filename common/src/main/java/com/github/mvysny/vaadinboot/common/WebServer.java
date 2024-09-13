package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;

/**
 * An abstraction over a web server, such as Jetty or Tomcat.
 * <br/>
 * The order of the calls:
 * <ul>
 *     <li>First {@link #configure(VaadinBootBase)} is called, to set up the server.</li>
 *     <li>Afterwards, {@link #start()} is called.</li>
 *     <li>Afterwards, optionally, {@link #await()} is called, to block the main thread.</li>
 *     <li>Finally, {@link #stop()} is called. Afterwards, the main method exits and the JVM will terminate.</li>
 * </ul>
 * <br/>
 * Every web server must provide the following:
 * <ul>
 *     <li>It must listen for http traffic on port {@link VaadinBootBase#port}, host {@link VaadinBootBase#hostName}, on {@link VaadinBootBase#contextRoot}</li>
 *     <li>It must serve static contents from <code>classpath://webapp</code>. There are utility functions in {@link Env} to locate the static contents and optionally unpack it to a temp folder</li>
 *     <li>WebSocket support</li>
 *     <li><code>@WebServlet</code> and <code>@WebListener</code> class auto-discovery, at least from the main app jar.</li>
 *     <li>Additional servlets besides Vaadin servlet, e.g. additional Javalin servlet</li>
 *     <li>Class hot-redeployment when JVM is running in debug mode. At least a basic hot-redeployment, as offered by the JVM, should be supported.</li>
 * </ul>
 * The following is not required:
 * <ul>
 *     <li>https support - we expect Traefik or Nginx front which will unwrap https, possibly refreshing certificates via ACME/Let's Encrypt.</li>
 *     <li>No <code>web.xml</code> is parsed</li>
 *     <li>The static content folder is served as-is: no modification is done to the files, no jsp compilation</li>
 *     <li>No JSP/JSF support</li>
 * </ul>
 */
public interface WebServer {
    /**
     * Configures the web server based on the configuration present in the {@link VaadinBootBase} object.
     * Called as the very first method.
     * <br/>
     * Will be called from one thread only, exactly once.
     * @param configuration the configuration.
     * @throws Exception if the configuration fails.
     */
    void configure(@NotNull VaadinBootBase<?> configuration) throws Exception;

    /**
     * Starts the web server. Blocks until both the web server and the app are fully initialized and started,
     * all servlets and web listeners have been called and initialized properly.
     * After the initialization is done and the web server is running, this function returns.
     * <br/>
     * Will be called from one thread only, exactly once.
     * @throws Exception if start fails, for example because the {@link VaadinBootBase#port port} is occupied.
     * In case of any exception, before this function quits, the web server must be fully stopped.
     */
    void start() throws Exception;

    /**
     * Stops the web server. Blocks until both the web server and the app are fully stopped,
     * all servlets and web listeners have been de-initialized properly.
     * After the de-initialization is done and the web server is stopped, this function returns.
     * <br/>
     * Will be called from one thread only, and at most once.
     * @throws Exception if stop failed.
     */
    void stop() throws Exception;

    /**
     * Can only be called on a started web server. Blocks until some other thread calls {@link #stop()};
     * this function returns after the de-initialization is done and the web server is stopped.
     * <br/>
     * May be called from multiple threads at the same time.
     * @throws InterruptedException if this thread was interrupted while waiting.
     */
    void await() throws InterruptedException;

    /**
     * Returns the name of this web server, e.g. "Tomcat" or "Jetty".
     * @return the name of this web server, e.g. "Tomcat" or "Jetty".
     */
    @NotNull String getName();
}
