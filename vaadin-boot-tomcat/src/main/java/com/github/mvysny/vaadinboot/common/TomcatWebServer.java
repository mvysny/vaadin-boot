package com.github.mvysny.vaadinboot.common;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

/**
 * Wraps a Tomcat web server:
 * <ul>
 *     <li>Homepage: <a href="https://tomcat.apache.org">Apache Tomcat</a></li>
 *     <li><a href="https://tomcat.apache.org/tomcat-10.1-doc/index.html">Documentation</a></li>
 *     <li>Embedded documentation: missing? or can't find the official one;
 *       <a href="https://devcenter.heroku.com/articles/create-a-java-web-application-using-embedded-tomcat">Heroku Embedded Tomcat docs</a>
 *     </li>
 * </ul>
 */
public class TomcatWebServer implements WebServer {
    private static final Logger log = LoggerFactory.getLogger(TomcatWebServer.class);

    // mark volatile: might be accessed by the shutdown hook from a different thread.
    private volatile Tomcat server;

    private volatile Context context;

    /**
     * The outcome of {@link Env#findResourcesJarOrFolder(URL)}.
     */
    protected volatile File resourcesJarOrFolder;
    /**
     * Cached outcome of {@link Env#findWebRoot()}.
     */
    protected volatile URL webRoot;

    /**
     * Creates a new instance of the wrapper. Only {@link com.github.mvysny.vaadinboot.VaadinBoot} is expected to call this.
     */
    public TomcatWebServer() {
    }

    /**
     * Returns the Tomcat {@link Context} object. Fails if {@link #configure(VaadinBootBase)} wasn't called yet.
     * @return Tomcat {@link Context} object.
     */
    @NotNull
    public Context getContext() {
        return Objects.requireNonNull(context);
    }

    /**
     * Returns the Tomcat embedded server. Fails if {@link #configure(VaadinBootBase)} wasn't called yet.
     * @return the Tomcat embedded server.
     */
    @NotNull
    public Tomcat getServer() {
        return Objects.requireNonNull(server);
    }

    @Override
    public void configure(@NotNull VaadinBootBase<?> configuration) throws Exception {
        webRoot = Env.findWebRoot();
        resourcesJarOrFolder = Env.findResourcesJarOrFolder(webRoot);

        server = new Tomcat();
        // first thing we need to do is to configure the basedir: if the basedir is configured
        // after connector is created, the setting will be ignored.
        final File basedir = Files.createTempDirectory("tomcat-" + configuration.port).toFile().getAbsoluteFile();
        server.setBaseDir(basedir.getAbsolutePath());
        log.debug("Tomcat basedir configured to " + basedir);
        server.setPort(configuration.port);
        server.setHostname(configuration.hostName == null ? "0.0.0.0" : configuration.hostName);
        server.getConnector(); // make sure the Connector is created so that Tomcat listens for http on 8080
        server.getConnector().setThrowOnFailure(true); // otherwise Tomcat would continue initializing even if 8080 was occupied.
        log.debug("Tomcat Connector created");

        context = createWebAppContext(configuration);
        log.debug("Tomcat Context created");
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
        server = null;
    }

    @Override
    public void await() throws InterruptedException {
        server.getServer().await();
    }

    @Override
    public @NotNull String getName() {
        return "Tomcat";
    }

    /**
     * Creates the Tomcat {@link Context}.
     * @param configuration the configuration to pass on to Tomcat.
     * @return the {@link Context}
     * @throws IOException on i/o error
     */
    @NotNull
    protected Context createWebAppContext(@NotNull VaadinBootBase<?> configuration) throws IOException {
        String contextRoot = configuration.contextRoot;
        if (contextRoot.equals("/")) {
            contextRoot = "";
        }
        // Create an empty folder. Tomcat wants to serve static files from a folder,
        // but we need to serve static files from classpath. Pass in an empty folder here -
        // we'll configure the static file serving later on.
        final String docBase = Files.createTempDirectory("tomcat-" + configuration.port).toFile().getAbsolutePath();

        final Context ctx = server.addWebapp(contextRoot, docBase);

        // in embedded mode there's just one webapp, and in that case the standard JVM class loading
        // makes more sense. Probably also improves JVM class hotswap.
        ctx.setLoader(new WebappLoader());
        ctx.getLoader().setDelegate(true);

        final WebResourceRoot root = new StandardRoot(ctx);
        // configure static file serving here.
        addStaticWebapp(root);
        enableClasspathScanning(root);
        ctx.setResources(root);
        return ctx;
    }

    /**
     * Configure the virtual WAR to serve static contents from {@link #resourcesJarOrFolder}'s <code>/webapp</code> package.
     * @param root the virtual WAR
     * @throws IOException on I/O error.
     */
    protected void addStaticWebapp(@NotNull WebResourceRoot root) throws IOException {
        if (resourcesJarOrFolder.isDirectory()) {
            root.addPreResources(new DirResourceSet(root, "/",
                    resourcesJarOrFolder.getAbsolutePath(), "/webapp"));
        } else {
            root.addPreResources(new JarResourceSet(root, "/",
                    resourcesJarOrFolder.getAbsolutePath(), "/webapp"));
        }
    }

    /**
     * To enable classpath scanning, we need to mount all app classes to the <code>WEB-INF/classes</code>
     * of the virtual WAR being created.
     * <br/>
     * Oddly enough, Vaadin's <code>LookupServletContainerInitializer</code> (which is annotated with
     * <code>@HandlesTypes</code>) <b>is</b> discovered and initialized, even though it's not present in <code>WEB-INF/classes</code>.
     * Even more odd, it fails to register the standard Vaadin Servlet if the app doesn't offer its own;
     * this is the reason why the app must define its own servlet at the moment.
     * <br/>
     * Alternative way would be to register the servlet manually via
     * <pre><code>
     * tomcat.addServlet("", "", VaadinServlet.class.getName());
     * ctx.addServletMappingDecoded("/*", "");
     * </code></pre>
     * But the app would need to add every servlet (e.g. Javalin servlet) and every
     * <code>@WebListener</code>; this would also break the requirement of {@link WebServer} interface
     * to have classpath scanning enabled.
     * @param root the virtual WAR
     * @throws IOException on I/O error.
     */
    protected void enableClasspathScanning(@NotNull WebResourceRoot root) throws IOException {
        // we need to add your app's classes to Tomcat to enable classpath scanning, in order to
        // auto-discover app @WebServlet and @WebListener.
        final Set<File> classesDirOrFolders = Env.findClassesJarOrFolder(webRoot);
        log.info("Classpath scanning enabled for " + classesDirOrFolders);
        if (classesDirOrFolders.isEmpty()) {
            throw new IllegalStateException("Invalid state: no class folders found");
        }
        for (File classesDirOrFolder : classesDirOrFolders) {
            if (classesDirOrFolder.isDirectory()) {
                root.addPreResources(new DirResourceSet(root, "/WEB-INF/classes", classesDirOrFolder.getAbsolutePath(), "/"));
            } else {
                root.addPreResources(new JarResourceSet(root, "/WEB-INF/classes", classesDirOrFolder.getAbsolutePath(), "/"));
            }
        }
    }
}
