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
    protected volatile URL webRoot;

    @NotNull
    public Context getContext() {
        return Objects.requireNonNull(context);
    }

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
     * @return the {@link Context}
     */
    @NotNull
    protected Context createWebAppContext(@NotNull VaadinBootBase<?> configuration) throws IOException {
        String contextRoot = configuration.contextRoot;
        if (contextRoot.equals("/")) {
            contextRoot = "";
        }
        final Context ctx = server.addWebapp(contextRoot, Files.createTempDirectory("tomcat-" + configuration.port).toFile().getAbsolutePath());
        // in embedded mode there's just one webapp, and in that case the standard JVM class loading
        // makes more sense. Probably also improves JVM class hotswap.
        ctx.setLoader(new WebappLoader());
        ctx.getLoader().setDelegate(true);

        final WebResourceRoot root = new StandardRoot(ctx);
        addStaticWebapp(root);
        enableClasspathScanning(root);
        ctx.setResources(root);
        return ctx;
    }

    protected void addStaticWebapp(@NotNull WebResourceRoot root) throws IOException {
        if (resourcesJarOrFolder.isDirectory()) {
            root.addPreResources(new DirResourceSet(root, "/",
                    resourcesJarOrFolder.getAbsolutePath(), "/webapp"));
        } else {
            root.addPreResources(new JarResourceSet(root, "/",
                    resourcesJarOrFolder.getAbsolutePath(), "/webapp"));
        }
    }

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
