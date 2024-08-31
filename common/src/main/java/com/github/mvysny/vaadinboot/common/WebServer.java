package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;

/**
 * An abstraction over a web server, such as Jetty or Tomcat. Thread-safe.
 */
public interface WebServer {
    /**
     * Configures the web server based on the configuration present in the {@link VaadinBootBase} object.
     * Called as the very first method.
     * @param configuration the configuration.
     * @throws Exception if the configuration fails.
     */
    void configure(@NotNull VaadinBootBase<?> configuration) throws Exception;

    /**
     * Starts the web server. Blocks until both the web server and the app are fully initialized and started,
     * all servlets and web listeners have been called and initialized properly.
     * After the initialization is done and the web server is running, this function returns.
     * @throws Exception if start fails.
     */
    void start() throws Exception;

    /**
     * Stops the web server. Blocks until both the web server and the app are fully stopped,
     * all servlets and web listeners have been de-initialized properly.
     * After the de-initialization is done and the web server is stopped, this function returns.
     * @throws Exception if stop failed.
     */
    void stop() throws Exception;

    /**
     * Can only be called on a started web server. Blocks until some other thread calls {@link #stop()};
     * this function returns after the de-initialization is done and the web server is stopped.
     * @throws InterruptedException if this thread was interrupted while waiting.
     */
    void await() throws InterruptedException;

    /**
     * Returns the name of this web server, e.g. "Tomcat" or "Jetty".
     * @return the name of this web server, e.g. "Tomcat" or "Jetty".
     */
    @NotNull String getName();
}
