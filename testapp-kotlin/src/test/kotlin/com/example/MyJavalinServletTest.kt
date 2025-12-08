package com.example

import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.Resource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.expect

/**
 * Vaadin is usually tested via Karibu-Testing and Jetty doesn't need to be running for that.
 * However, in order to test the REST, we need to start Javalin in Jetty.
 */
class MyJavalinServletTest {
    private var server: Server? = null

    @BeforeEach
    fun startJetty() {
        val ctx = WebAppContext()
        ctx.baseResource = EmptyResource()
        expect(false) { ctx.baseResource == null }
        ctx.addServlet(MyJavalinServlet::class.java, "/rest/*")
        server = Server(30123)
        server!!.handler = ctx
        server!!.start()
    }

    @AfterEach
    fun stopJetty() {
        server?.stop()
    }

    @Test
    fun testRest() {
        assertEquals("Hello!", URI("http://localhost:30123/rest").toURL().readText())
    }
}

class EmptyResource : Resource() {
    override fun getPath(): Path? = null
    override fun isDirectory(): Boolean = true
    override fun isReadable(): Boolean = true
    override fun getURI(): URI? = null
    override fun getName(): String = "EmptyResource"
    override fun getFileName(): String? = null
    override fun resolve(subUriPath: String?): Resource? = null
}
