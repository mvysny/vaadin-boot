package com.github.mvysny.vaadinboot;

import org.eclipse.jetty.quickstart.ExtraXmlDescriptorProcessor;
import org.eclipse.jetty.quickstart.QuickStartGeneratorConfiguration;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Helper methods for Jetty Quick Start mode.
 */
class JettyQuickStart {
    public static boolean quickstartXmlExists(@NotNull Resource webroot) {
        try {
            return webroot.getResource("WEB-INF/quickstart-web.xml").exists();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate the Jetty QuickStart config file to current working directory.
     * @param context Jetty webapp. The classpath scanning is only performed when
     *                the Jetty server actually starts; therefore you must start Jetty in order for this function to work properly.
     * @throws IOException on I/O error
     */
    public static void generateQuickStartXml(@NotNull WebAppContext context) throws IOException {
        context.setAttribute(ExtraXmlDescriptorProcessor.class.getName(), new ExtraXmlDescriptorProcessor());
        final String xml = new File("quickstart-web.xml").getAbsolutePath();
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(xml))) {
            new QuickStartGeneratorConfiguration().generateQuickStartWebXml(context, out);
        }
        log.info("Generated Jetty QuickStart configuration file at " + xml + ". Place the file into src/main/resources/webapp/WEB-INF/quickstart-web.xml of your webapp and ");
    }

    private static final Logger log = LoggerFactory.getLogger(JettyQuickStart.class);
}
