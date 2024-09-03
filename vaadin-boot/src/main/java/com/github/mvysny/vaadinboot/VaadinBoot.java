package com.github.mvysny.vaadinboot;

import com.github.mvysny.vaadinboot.common.JettyWebServer;
import com.github.mvysny.vaadinboot.common.VaadinBootBase;
import com.github.mvysny.vaadinboot.common.WebServer;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
public class VaadinBoot extends VaadinBootBase<VaadinBoot> {
    /**
     * If true, no classpath scanning is performed - no servlets nor weblisteners are detected.
     * <br/>
     * This will most probably cause Vaadin to not work and throw NullPointerException at <code>VaadinServlet.serveStaticOrWebJarRequest</code>.
     * However, it's a good thing to disable this when starting your app with a QuickStart configuration.
     */
    private boolean disableClasspathScanning = false;

    /**
     * If true, the test classpath will also be scanned for annotations. Defaults to false.
     * <br/>
     * Only set to true if you have Vaadin routes in <code>src/test/java/</code> - it's
     * a bit of an antipattern but quite common with Vaadin addons. See
     * <a href="https://github.com/mvysny/vaadin-boot/issues/15">Issue #15</a> for more details.
     * <br/>
     * Ignored if {@link #disableClasspathScanning} is true.
     */
    private boolean isScanTestClasspath = false;

    /**
     * If true and we're running on JDK 21+, we'll configure Jetty to take advantage
     * of virtual threads.
     * <br/>
     * Defaults to true.
     */
    private boolean useVirtualThreadsIfAvailable = true;

    /**
     * Creates new boot instance.
     */
    public VaadinBoot() {
        super(new JettyWebServer());
    }

    /**
     * If true, no classpath scanning is performed - no servlets nor weblisteners are detected.
     * <br/>
     * This will most probably cause Vaadin to not work and throw NullPointerException at <code>VaadinServlet.serveStaticOrWebJarRequest</code>.
     * However, it's a good thing to disable this when starting your app with a QuickStart configuration.
     * @return this
     */
    @NotNull
    public VaadinBoot disableClasspathScanning() {
        return disableClasspathScanning(true);
    }

    /**
     * If true, no classpath scanning is performed - no servlets nor weblisteners are detected.
     * <br/>
     * This will most probably cause Vaadin to not work and throw NullPointerException at <code>VaadinServlet.serveStaticOrWebJarRequest</code>.
     * However, it's a good thing to disable this when starting your app with a QuickStart configuration.
     * @param disableClasspathScanning If true, no classpath scanning is performed. Defaults to false.
     * @return this
     */
    @NotNull
    public VaadinBoot disableClasspathScanning(boolean disableClasspathScanning) {
        this.disableClasspathScanning = disableClasspathScanning;
        return this;
    }

    /**
     * See {@link #disableClasspathScanning()}.
     * @return If true, no classpath scanning is performed - no servlets nor weblisteners are detected.
     */
    public boolean isDisableClasspathScanning() {
        return disableClasspathScanning;
    }

    /**
     * When called, the test classpath will also be scanned for annotations. Defaults to false.
     * <br/>
     * Use only in case when you have Vaadin routes in <code>src/test/java/</code> - it's
     * a bit of an antipattern but quite common with Vaadin addons. See
     * <a href="https://github.com/mvysny/vaadin-boot/issues/15">Issue #15</a> for more details.
     * <br/>
     * Ignored if {@link #disableClasspathScanning} is true.
     * @return this
     */
    @NotNull
    public VaadinBoot scanTestClasspath() {
        isScanTestClasspath = true;
        return this;
    }

    /**
     * See {@link #scanTestClasspath()}.
     * @return if true, the test classpath will also be scanned for annotations. Defaults to false.
     */
    public boolean isScanTestClasspath() {
        return isScanTestClasspath;
    }

    /**
     * If true and we're running on JDK 21+, we'll configure Jetty to take advantage
     * of virtual threads.
     * <br/>
     * Defaults to true.
     * @param useVirtualThreadsIfAvailable if true (default), use virtual threads to
     *                                     handle http requests if running on JDK21+
     * @return this
     */
    @NotNull
    public VaadinBoot useVirtualThreadsIfAvailable(boolean useVirtualThreadsIfAvailable) {
        this.useVirtualThreadsIfAvailable = useVirtualThreadsIfAvailable;
        return this;
    }

    /**
     * If true and we're running on JDK 21+, we'll configure Jetty to take advantage
     * of virtual threads. See {@link #useVirtualThreadsIfAvailable(boolean)}.
     * @return If true and we're running on JDK 21+, we'll configure Jetty to take advantage
     * of virtual threads.
     */
    public boolean isUseVirtualThreadsIfAvailable() {
        return useVirtualThreadsIfAvailable;
    }

    @Override
    protected void onStarted(@NotNull WebServer server) throws IOException {
        onStarted(((JettyWebServer) server).getContext());
    }

    /**
     * Invoked when the Jetty server has been started. By default, does nothing. You can
     * for example dump the quickstart configuration here.
     * @param context the web app context.
     * @throws IOException on i/o exception
     */
    protected void onStarted(@NotNull WebAppContext context) throws IOException {
    }
}
