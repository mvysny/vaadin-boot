package com.example

import org.apache.catalina.startup.Tomcat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Files
import kotlin.test.assertEquals

/**
 * Vaadin is usually tested via Karibu-Testing and Tomcat doesn't need to be running for that.
 * However, in order to test the REST, we need to start Javalin in Tomcat.
 */
class MyJavalinServletTest {
    private var server: Tomcat? = null

    @BeforeEach
    fun startTomcat() {
        server = Tomcat()
        // first thing we need to do is to configure the basedir: if the basedir is configured
        // after connector is created, the setting will be ignored.
        val basedir =
            Files.createTempDirectory("tomcat-30123")
                .toFile().absoluteFile
        server!!.setBaseDir(basedir.absolutePath)
        server!!.setPort(30123)
        server!!.setHostname("127.0.0.1")
        server!!.getConnector()
        server!!.getConnector().throwOnFailure = true

        // Create an empty folder. Tomcat wants to serve static files from a folder,
        // but we need to serve static files from classpath. Pass in an empty folder here -
        // we'll configure the static file serving later on.
        val docBase =
            Files.createTempDirectory("tomcat-30123-docbase").toFile()
                .absolutePath
        val ctx = server!!.addWebapp("", docBase)
        Tomcat.addServlet(ctx, "restServlet", MyJavalinServlet())
        ctx.addServletMappingDecoded("/rest/*", "restServlet")
        server!!.start()
    }

    @AfterEach
    fun stopTomcat() {
        server?.stop()
    }

    @Test
    fun testRest() {
        assertEquals(
            "Hello!",
            URI("http://localhost:30123/rest").toURL().readText()
        )
    }
}

