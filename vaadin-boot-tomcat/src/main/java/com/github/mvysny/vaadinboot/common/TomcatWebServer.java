package com.github.mvysny.vaadinboot.common;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TomcatWebServer implements WebServer {
    private static final Logger log = LoggerFactory.getLogger(TomcatWebServer.class);

    // mark volatile: might be accessed by the shutdown hook from a different thread.
    private volatile Tomcat server;

    private volatile Context context;

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
    protected Context createWebAppContext(@NotNull VaadinBootBase<?> configuration) {
        final File webappFolder = getWebappFolder();

        String contextRoot = configuration.contextRoot;
        if (contextRoot.equals("/")) {
            contextRoot = "";
        }
        final Context ctx = server.addWebapp(contextRoot, webappFolder.getAbsolutePath());

        // we need to add classes to Tomcat to enable classpath scanning, in order to
        // auto-discover app @WebServlet and @WebListener.
        final File classDirMaven = new File("target/classes").getAbsoluteFile();
        final File classDirGradle = new File("build/classes").getAbsoluteFile();
        File additionWebInfClasses = classDirMaven;  // dev env with Maven
        if (!additionWebInfClasses.exists()) {
            additionWebInfClasses = classDirGradle;  // dev env with Gradle
        }
        if (additionWebInfClasses.exists()) {
            final WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        } else {
            final File libs = new File("../lib").getAbsoluteFile();
            if (!libs.exists()) {
                throw new IllegalStateException("Invalid state: " + libs + " does not exist");
            }
            final String mainJarNameRegex = ((VaadinBoot) configuration).mainJarNameRegex;
            final File[] possibleProductionJarFilesArray = libs.listFiles((dir, name) -> name.matches(mainJarNameRegex));
            final List<File> possibleProductionJarFiles = possibleProductionJarFilesArray == null ? Collections.emptyList() : Arrays.asList(possibleProductionJarFilesArray);
            if (possibleProductionJarFiles.size() != 1) {
                throw new IllegalStateException("Invalid state: expected exactly one app jar file " + mainJarNameRegex + " but got " + possibleProductionJarFiles);
            }
            final File productionJar = possibleProductionJarFiles.get(0);
            if (!productionJar.exists()) {
                throw new IllegalStateException("Invalid state: " + productionJar + " doesn't exist");
            }
            final WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new JarResourceSet(resources, "/WEB-INF/classes",
                    productionJar.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }
        return ctx;
    }

    private static @NotNull File getWebappFolder() {
        final File webappFolderDev = new File("src/dist/webapp").getAbsoluteFile();
        final File webappFolderProd = new File("../webapp").getAbsoluteFile();
        File docBase = webappFolderDev;
        if (!docBase.exists()) {
            docBase = webappFolderProd;
        }
        if (!docBase.exists()) {
            throw new IllegalStateException("Invalid state: The webapp folder isn't present neither at " + webappFolderDev + " (development mode) nor at " + webappFolderProd + " (production)");
        }
        return docBase;
    }
}
